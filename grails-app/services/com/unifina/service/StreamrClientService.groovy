package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.authentication.ApiKeyAuthenticationMethod
import com.streamr.client.authentication.AuthenticationMethod
import com.streamr.client.authentication.EthereumAuthenticationMethod
import com.streamr.client.options.EncryptionOptions
import com.streamr.client.options.SigningOptions
import com.streamr.client.options.StreamrClientOptions
import com.unifina.domain.security.Key
import com.unifina.utils.MapTraversal
import grails.util.Holders

import java.lang.reflect.Constructor

class StreamrClientService {

	StreamrClient instanceForThisEngineNode
	Constructor<StreamrClient> clientConstructor

	StreamrClientService() {
		clientConstructor = StreamrClient.class.getConstructor(StreamrClientOptions)
	}

	/**
	 * Useful for testing, this constructor allows the StreamrClient class to be injected
	 */
	StreamrClientService(Class<StreamrClient> streamrClientClass) {
		clientConstructor = streamrClientClass.getConstructor(StreamrClientOptions)
	}

	/**
	 * Returns a StreamrClient instance, authenticated with one of the provided user's
	 * API keys. This method fetches from the centralized database an API key to be
	 * used with the StreamrClient.
	 *
	 * Whoever calls this should take care of closing the client when it is no longer needed.
	 */
	StreamrClient getAuthenticatedInstance(Long userIdToAuthenticate) {
		// Uses superpowers to get an API key for the user to authenticate the data
		return createInstance(new ApiKeyAuthenticationMethod(getApiKeyForUser(userIdToAuthenticate)))
	}

	/**
	 * Returns a shared StreamrClient instance authenticated with the private key
	 * of this Engine node. Other services needing to publish messages as this node
	 * may use this instance to do so.
	 *
	 * This is a long running instance which should not be closed as long as this
	 * application is running. Whoever calls this method should not close the instance.
	 */
	StreamrClient getInstanceForThisEngineNode() {
		if (!instanceForThisEngineNode) {
			String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
			instanceForThisEngineNode = createInstance(new EthereumAuthenticationMethod(nodePrivateKey))
		}
		return instanceForThisEngineNode
	}

	private StreamrClient createInstance(AuthenticationMethod authenticationMethod) {
		StreamrClientOptions options = new StreamrClientOptions(
			authenticationMethod,
			SigningOptions.getDefault(),
			EncryptionOptions.getDefault(),
			MapTraversal.getString(Holders.getConfig(), "streamr.api.websocket.url"),
			MapTraversal.getString(Holders.getConfig(), "streamr.api.http.url")
		)
		return clientConstructor.newInstance(options)
	}

	/**
	 * Returns an API key for the given user. This is used
	 * by Canvases to subscribe to the Streams required by the Canvas.
	 *
	 * Currently, the first returned key is chosen. It would be better
	 * if the user could specify which key to use to run their Canvases
	 * by marking one key as "default", or offering a choice
	 * in Canvas run settings.
	 */
	private String getApiKeyForUser(Long userId) {
		List<Key> keys = Key.where {
			user.id == userId
		}.findAll()

		if (keys.isEmpty()) {
			throw new IllegalStateException("User does not have an API key! This should not happen!")
		} else {
			return keys[0].id
		}
	}
}

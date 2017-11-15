import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import IntegrationKeyHandlerTable from '../../../../../../components/ProfilePage/IntegrationKeyHandler/IntegrationKeyHandlerSegment/IntegrationKeyHandlerTable/index'

describe('IntegrationKeyHandlerTable', () => {
    
    describe('render', () => {
        describe('thead', () => {
            it('renders header correctly', () => {
                const el = shallow(<IntegrationKeyHandlerTable
                    fields={['Test']}
                    integrationKeys={[]}
                    onDelete={() => {
                    }}
                />)
                assert(el.is('.integrationKeyTable'))
                const thead = el.childAt(0)
                const tr = thead.childAt(0)
                assert.equal(tr.find('th').length, 3)
                assert.equal(tr.find('th')
                    .at(0)
                    .text(), 'Name')
                assert(tr.find('th')
                    .at(0)
                    .is('.nameHeader'))
                assert.equal(tr.find('th')
                    .at(1)
                    .text(), 'Test')
                assert.equal(tr.find('th')
                    .at(2)
                    .text(), '')
                assert(tr.find('th')
                    .at(2)
                    .is('.actionHeader'))
            })
            it('renders fields in title case', () => {
                const el = shallow(<IntegrationKeyHandlerTable
                    fields={['firstCamelCase', 'secondCamelCase']}
                    integrationKeys={[]}
                    onDelete={() => {
                    }}
                />)
                const thead = el.childAt(0)
                const tr = thead.childAt(0)
                assert.equal(tr.find('th')
                    .at(1)
                    .text(), 'First Camel Case')
                assert.equal(tr.find('th')
                    .at(2)
                    .text(), 'Second Camel Case')
            })
        })
        
        describe('tbody', () => {
            it('must render IntegrationKeyHandlerTableRow fro every item', () => {
                const el = shallow(<IntegrationKeyHandlerTable
                    fields={['firstCamelCase', 'secondCamelCase']}
                    integrationKeys={[]}
                    onDelete={() => {
                    }}
                />)
                const thead = el.childAt(0)
                const tr = thead.childAt(0)
                assert.equal(tr.find('th')
                    .at(1)
                    .text(), 'First Camel Case')
                assert.equal(tr.find('th')
                    .at(2)
                    .text(), 'Second Camel Case')
            })
        })
    })
})

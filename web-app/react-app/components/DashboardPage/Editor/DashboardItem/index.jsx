// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import path from 'path'

import TitleRow from './DashboardItemTitleRow'

import styles from './dashboardItem.pcss'

import type {Dashboard, DashboardItem as DBItem} from '../../../../types/dashboard-types'

declare var Streamr: {}

class DashboardItem extends Component {
    
    props: {
        item: DBItem,
        dashboard: Dashboard,
        packery: any,
        layout?: DBItem.layout,
        dragCancelClassName?: string
    }
    
    componentDidMount() {
    }
    
    render() {
        const item = this.props.item || {}
        const WebComponent = item.webcomponent
        return (
            <div className={styles.dashboardItem}>
                <div className={styles.header}>
                    <TitleRow dashboard={this.props.dashboard} item={item} dragCancelClassName={this.props.dragCancelClassName}/>
                </div>
                <div className={`${styles.body} ${this.props.dragCancelClassName || ''}`}>
                    <div className={styles.wrapper}>
                        <WebComponent
                            className="streamr-widget non-draggable"
                            url={Streamr.createLink({
                                uri: path.resolve('api/v1/dashboards', item.dashboard.toString(), 'canvases', item.canvas.toString(), 'modules', item.module.toString())
                            })}
                        />
                    </div>
                </div>
            </div>
        )
    }
}

export default connect()(DashboardItem)
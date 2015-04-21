/** @jsx React.DOM */
var ResourceEditor = React.createClass({
    getInitialState: function() {
        return {
            groupData: null,
            currentGroupName: null,
            resourceData:null,
            currentResourceName: null,
            resourceEditMode: false,
            resourceTypes:[]
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading group data...</div>
        }

        var treeMetaData = [{propKey: "name", selectable: true}];
        var groupJvmTreeList = <RStaticDialog ref="groupsDlg"
                                              title="Groups"
                                              contentClassName="resource-static-dialog-content">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectGroupNodeCallback} />
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog title="Resources" contentClassName="resource-static-dialog-content">
                                <ResourcePane groupName={this.state.currentGroupName}
                                              data={this.state.resourceData}
                                              insertNewResourceCallback={this.insertNewResourceCallback}
                                              deleteResourcesCallback={this.deleteResourcesCallback}
                                              updateResourceCallback={this.updateResourceCallback}
                                              currentResourceName={this.state.currentResourceName}
                                              editMode={this.state.resourceEditMode}
                                              selectResourceCallback={this.selectResourceCallback}
                                              resourceTypes={this.state.resourceTypes}
                                              onChangeResourceListItem={this.onChangeResourceListItem}/>
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog title="Attributes and Values" contentClassName="resource-static-dialog-content">
                                   <ResourceAttrPane ref="resourceAttrEditor"
                                                     resourceData={this.getCurrentResource()}
                                                     updateCallback={this.updateAttrCallback}
                                                     requiredAttributes={this.getRequiredAttributes()}
                                                     generateXmlSnippetCallback={this.props.generateXmlSnippetCallback}/>
                               </RStaticDialog>

        var splitterComponents = [];
        splitterComponents.push(<div className="resource-pane">{groupJvmTreeList}</div>);
        splitterComponents.push(<div className="resource-pane">{resourcesPane}</div>);
        splitterComponents.push(<div className="resource-pane">{resourceAttrPane}</div>);

        return <RSplitter components={splitterComponents}
                          orientation={RSplitter.HORIZONTAL_ORIENTATION}
                          panelDimensions={[{width:"33.33%", height:"100%"},
                                            {width:"33.33%", height:"100%"},
                                            {width:"33.33%", height:"100%"}]} />
    },
    onChangeResourceListItem: function(resourceTypeName, resourceName, newResourceName) {
        resourceService.updateResourceName(this.state.currentGroupName,
                                           resourceTypeName,
                                           resourceName,
                                           newResourceName,
                                           this.updateResourceSuccessCallback.bind(this, newResourceName),
                                           this.updateResourceErrorCallback);
    },
    updateResourceSuccessCallback: function(newResourceName) {
        this.updateResourceCallback(newResourceName);
    },
    updateResourceErrorCallback: function(errMsg) {
         $.errorAlert(errMsg, "Error");
    },
    selectResourceResponseCallback: function(response) {
        this.props.getTemplateCallback(response.applicationResponseContent);
    },
    getRequiredAttributes: function() {
        var requiredAttributes = [];
        if (this.state.currentResourceName !== null) {
            var currentResource = this.getCurrentResource();
            if (currentResource !== null) {
                for (var i = 0; i < this.state.resourceTypes.length; i++) {
                    if (this.state.resourceTypes[i].name === currentResource.resourceTypeName) {
                        for (var ii = 0; ii < this.state.resourceTypes[i].properties.length; ii++) {
                            if (this.state.resourceTypes[i].properties[ii].required === "true") {
                                requiredAttributes.push(this.state.resourceTypes[i].properties[ii].name);
                            }
                        }
                        return requiredAttributes;
                    }
                }
            }
        }
        return [];
    },
    componentDidMount: function() {
        this.props.groupService.getGroups(this.getGroupDataCallback);
        this.props.resourceService.getResourceTypes(this.getResourceTypesCallback);
    },
    getGroupDataCallback: function(response) {
        this.setState({groupData:response.applicationResponseContent});
    },
    getResourceTypesCallback: function(response) {
        this.setState({resourceTypes:response.applicationResponseContent});
    },
    componentDidUpdate: function() {
        if (this.state.currentGroupName === null && this.state.groupData !== null && this.state.groupData.length > 0) {
            // Select the first group
            this.refs.treeList.selectNode(this.state.groupData[0].name);
            this.setState({currentGroupName:this.state.groupData[0].name});
        }
    },
    refreshResourceListResponseCallback: function(resource) {
    },
    selectGroupNodeCallback: function(group) {
        this.setState({currentGroupName:group.name});
    },
    componentWillUpdate: function(nextProps, nextState) {
        if (this.state.currentGroupName != nextState.currentGroupName) {
            // Retrieve resources
            this.props.resourceService.getResources(nextState.currentGroupName, this.getResourceDataCallback);
        }
    },
    getResourceDataCallback: function(response) {
        this.setState({resourceData:response.applicationResponseContent});
    },
    insertNewResourceCallback: function(resourceName) {
        this.props.resourceService.getResources(this.state.currentGroupName,
            this.getResourceDataCallbackExt.bind(this, resourceName, true));
    },
    deleteResourcesCallback: function() {
        this.props.resourceService.getResources(this.state.currentGroupName, this.getResourceDataCallback);
    },
    updateResourceCallback: function(newResourceName) {
        this.props.resourceService.getResources(this.state.currentGroupName,
              this.getResourceDataCallbackExt.bind(this, newResourceName, false));
    },
    getResourceDataCallbackExt: function(resourceName, editMode, response) {
        if (editMode) {
            // Adding of a new resource sets the edit mode to true. The requirement is that the recently added
            // resources should be selected and is in edit mode thus we set edit mode to true and set the current
            // resource to the newly added resource.
            this.setState({currentResourceName:resourceName,
                           resourceEditMode:editMode,
                           resourceData:response.applicationResponseContent});
        } else {
            // Don't set current resource here since it will interfere with the select event!
            // The code goes here when there's an update triggered by a blur event (e.g. another resource was selected).
            this.setState({resourceEditMode:editMode,
                           resourceData:response.applicationResponseContent});
        }
    },
    selectResourceCallback: function(resource) {
        this.setState({currentResourceName:resource.name});
        this.props.resourceService.getTemplate(resource.resourceTypeName, this.selectResourceResponseCallback);
    },
    getCurrentResource: function() {
        if (this.state.resourceData !== null) {
            for (var i = 0;i < this.state.resourceData.length;i++) {
                if (this.state.resourceData[i].name === this.state.currentResourceName) {
                    return this.state.resourceData[i];
                }
            }
        }
        return null;
    },
    updateAttrCallback: function() {
        this.props.resourceService.getResources(this.state.currentGroupName, this.getResourceDataCallback);
    }
});
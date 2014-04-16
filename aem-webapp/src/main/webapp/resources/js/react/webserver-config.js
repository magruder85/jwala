/** @jsx React.DOM */
var WebServerConfig = React.createClass({
    getInitialState: function() {
        selectedWebServer = null;
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            webServerFormData: {},
            webServerTableData: [{"name":"","id":{"id":0}}],
            groupMultiSelectData: []
        }
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className}>
                    <table>
                        <tr>
                            <td>
                                <div>
                                    <GenericButton label="Delete" callback={this.delBtnCallback}/>
                                    <GenericButton label="Add" callback={this.addBtnCallback}/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div>
                                    <WebServerDataTable data={this.state.webServerTableData}
                                                    selectItemCallback={this.selectItemCallback}
                                                    editCallback={this.editCallback}
                                                    noUpdateWhen={
                                                      this.state.showModalFormAddDialog || 
                                                      this.state.showDeleteConfirmDialog ||
                                                      this.state.showModalFormEditDialog 
                                                      }/>
                                </div>
                            </td>
                        </tr>
                   </table>
                   <ModalFormDialog title="Add Web Server"
                                    show={this.state.showModalFormAddDialog}
                                    form={<WebServerConfigForm service={this.props.service}
                                                               groupMultiSelectData={this.state.groupMultiSelectData}/>}
                                    successCallback={this.addEditSuccessCallback}
                                    destroyCallback={this.closeModalFormAddDialog}
                                    className="textAlignLeft"/>
                   <ModalFormDialog title="Edit Web Server"
                                    show={this.state.showModalFormEditDialog}
                                    form={<WebServerConfigForm service={this.props.service}
                                                           data={this.state.webServerFormData}
                                                           groupMultiSelectData={this.state.groupMultiSelectData}/>}
                                    successCallback={this.addEditSuccessCallback}
                                    destroyCallback={this.closeModalFormEditDialog}
                                    className="textAlignLeft"/>
                   <ConfirmDeleteModalDialog show={this.state.showDeleteConfirmDialog}
                                             btnClickedCallback={this.confirmDeleteCallback} />
               </div>
    },
    confirmDeleteCallback: function(ans) {
        var self = this;
        this.setState({showDeleteConfirmDialog: false});
        if (ans === "yes") {
            this.props.service.deleteWebServer(selectedWebServer.id.id).then(
                function(){
                },
                function(response) {
                    if (response.status !== 200) {
                        $.errorAlert(JSON.stringify(response), "Error");
                    }
                    self.retrieveData();
                }
            );
        }
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getWebServers().then(
            function(response){
                self.setState({webServerTableData:response.applicationResponseContent});
            },
            function(response) {
                $.errorAlert(JSON.stringify(response), "Error");
            }
        );

        groupService.getGroups().then(
            function(response){
                self.setState({groupMultiSelectData:response.applicationResponseContent});
            },
            function(response) {
                $.errorAlert(JSON.stringify(response), "Error");
            }
        );
    },
    addEditSuccessCallback: function() {
        this.retrieveData();
        return true;
    },
    addBtnCallback: function() {
        this.setState({showModalFormAddDialog: true})
    },
    delBtnCallback: function() {
        if (selectedWebServer != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    selectItemCallback: function(item) {
        selectedWebServer = item;
    },
    editCallback: function(id) {
        var thisComponent = this;
        this.props.service.getWebServer(id).then(
            function(response){
                thisComponent.setState({webServerFormData: response.applicationResponseContent,
                                        showModalFormEditDialog: true})
            },
            function(response) {
                $.errorAlert(JSON.stringify(response), "Error");
            }
        );
    },
    closeModalFormAddDialog: function() {
        this.setState({showModalFormAddDialog: false})
    },
    closeModalFormEditDialog: function() {
        this.setState({showModalFormEditDialog: false})
    },
    componentDidMount: function() {
        // this.retrieveData();
    },
    componentWillMount: function() {
        this.retrieveData();
    }
});

var WebServerConfigForm = React.createClass({
    getInitialState: function() {
        var webServerId = "";
        var webServerName = "";

        if (this.props.data !== undefined) {
            webServerId = this.props.data.id.id;
            webServerName = this.props.data.name;
        }
        return {
            validator: null,
            webServerId: webServerId,
            webServerName: webServerName
        }
    },
    render: function() {
        var self = this;
        return <form action="v1.0/webServers">
                    <input name="id" type="hidden" defaultValue={this.state.webServerId} />
                    <table>
                        <tr>
                            <td>Name</td>
                        </tr>
                        <tr>
                            <td>
                                <label htmlFor="name" className="error"></label>
                            </td>
                        </tr>
                        <tr>
                            <td><input name="name" type="text" defaultValue={this.state.webServerName} required/></td>
                        </tr>
                        <tr>
                            <td>Host</td>
                        </tr>
                        <tr>
                            <td>
                                <label htmlFor="host" className="error"></label>
                            </td>
                        </tr>
                        <tr>
                            <td><input name="host" type="text" defaultValue={this.state.webServerHost} required/></td>
                        </tr>
                        <tr>
                            <td>Port</td>
                        </tr>
                        <tr>
                            <td>
                                <label htmlFor="port" className="error"></label>
                            </td>
                        </tr>
                        <tr>
                            <td><input name="port" type="text" defaultValue={this.state.webServerPort} required/></td>
                        </tr>
                        <tr>
                            <td>
                                Group
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label htmlFor="groupSelector[]" className="error"></label>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <DataMultiSelectBox name="groupSelector[]"
                                                    data={this.props.groupMultiSelectData}
                                                    key="id"
                                                    keyPropertyName="id"
                                                    val="name"
                                                    className="data-multi-select-box"/>
                            </td>
                        </tr>

                    </table>
               </form>
    },
    componentDidMount: function() {

        var validator = $(this.getDOMNode()).validate({
            ignore: ":hidden",
            rules: {
                "groupSelector[]": {
                    required: true
                }
            },
            messages: {
                "groupSelector[]": {
                    required: "Please select at least 1 group"
                }
            }
        });

        this.setState({validator:validator});
    },
    submit: function(done, fail) {
        var thisComponent = this;
        var svc = thisComponent.props.service;
        var data = thisComponent.props.data;

        $(this.getDOMNode()).one("submit", function(e) {

            if (data === undefined) {

                var groupIds = [];
                $("input[name='groupSelector[]']").each(function () {
                    if ($(this).prop("checked")) {
                        groupIds.push({groupId:$(this).val()});
                    }
                });

                svc.insertNewWebServer($("input[name=name]").val(),
                                       groupIds,
                                       $("input[name=host]").val(),
                                       $("input[name=port]").val()).then(
                    function(){
                        done();
                    },
                    function(response) {
                        fail(JSON.parse(response.responseText).applicationResponseContent);
                    }
                );
            } else {
                svc.updateWebServer($(thisComponent.getDOMNode()).serializeArray()).then(
                    function(){
                        done();
                    },
                    function(response) {
                        fail(JSON.parse(response.responseText).applicationResponseContent);
                    }
                );
            }

            e.preventDefault(); // stop the default action
        });

        if (this.state.validator !== null) {
            this.state.validator.cancelSubmit = true;
            this.state.validator.form();
            if (this.state.validator.numberOfInvalids() === 0) {
                $(this.getDOMNode()).submit();
            }
        } else {
            alert("There is no validator for the form!");
        }

    }
});

var WebServerDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {
    
      return !nextProps.noUpdateWhen;
        
    },
    render: function() {
        var headerExt = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                         {sTitle:"Name", mData:"name", tocType:"link"},
                         {sTitle:"Host", mData:"host"},
                         {sTitle:"Port", mData:"port"},
                         {sTitle:"Group Assignment",
                          mData:"groups",
                          tocType:"array",
                          displayProperty:"name"}];
        return <TocDataTable2 theme="default"
                              headerExt={headerExt}
                              colHeaders={["JVM Name", "Host Name"]}
                              data={this.props.data}
                              selectItemCallback={this.props.selectItemCallback}
                              editCallback={this.props.editCallback}
                              expandIcon="public-resources/img/react/components/details-expand.png"
                              collapseIcon="public-resources/img/react/components/details-collapse.png"
                              rowSubComponentContainerClassName="row-sub-component-container"/>
    }
});
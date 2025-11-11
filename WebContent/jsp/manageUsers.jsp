<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags" %>
<script
    src="/M9000Master/struts/utils.js"
    type="text/javascript">
</script>
<script
    src="/M9000Master/struts/xhtml/validation.js"
    type="text/javascript">
</script>

<script>
    /*Destroy previous instances of jQuery Dialogs  */
	$(".ui-dialog-content").each(function(i,elt){
		if ($(elt).dialog){
			  $(this).dialog('destroy').remove();		  
		}
	});
</script>
<s:url var="remoteurl" action="usersList-json" />
<sjdt:datatables id="editableTable" datatablesTheme="jqueryui"
	ajaxReloadTopics="rowSavedTopic,rowDeletedTopic" pageTopics="testTopic"
	buttons="[
{extend:'selectedSingle',text:'Edit',action:function(){$(this).publish('editTopic');}},
{text:'Create',action:function(){$(this).publish('createTopic');}},
{extend:'selectedSingle',text:'Delete',action:function(){$(this).publish('deleteTopic');}}
]"
	dom="Blfrtip" ajax="{url:'%{remoteurl}',dataSrc:'m9kAppUsers'}"
	select="'single'"
	columns="[
			{data:'id',title:'Id', className: 'dt-body-center'},
            {data:'userName',title:'Name', className: 'dt-body-center'},
            {data:'role',title:'Role', className: 'dt-body-center'}
]"
	responsive="true" style="width:100%;">
	<caption class="ui-widget-header">Manage Users</caption>
	<thead>
		<tr>
			<th>Id</th>
			<th>Name</th>
			<th>Role</th>
		</tr>
	</thead>
</sjdt:datatables>
<sj:dialog id="editDialog" autoOpen="false" title="Edit User"
	openTopics="editTopic,createTopic" closeTopics="rowSavedTopic">
	<s:form id="editForm" theme="xhtml" action="saveUsers">
		<s:hidden id="idUsers" name="id" />
		<s:textfield id="userName" name="userName" label="Name" />
		<s:password id="password" name="password" label="Password" />
		<s:select id="role" name="role" list="{'guest','admin'}" label="Role" />
		<sj:submit value="Submit" button="true" buttonText="Submit"
			validate="true" dataType="json" targets="x"
			onSuccessTopics="rowSavedTopic" onErrorTopics="rowSavedErrorTopic" />
	</s:form>
</sj:dialog>
<sj:dialog id="deleteDialog" autoOpen="false" title="Delete"
	openTopics="deleteTopic" closeTopics="rowDeletedTopic" modal="true">
	<s:form id="deleteForm" theme="simple" action="deleteUsers">
		<s:hidden id="idUsers" name="id" />
        Delete the row ?
        <div class="type-button">
			<sj:submit value="Confirm" button="true" targets="x" dataType="json"
				buttonText="Confirm" onSuccessTopics="rowDeletedTopic"
				onErrorTopics="rowDeletedErrorTopic" />
			<sj:a onClickTopics="rowDeletedTopic" button="true">Cancel</sj:a>
		</div>
	</s:form>
</sj:dialog>


<script>
	var $editForm = null;
	$(function() {
		$editForm = $("#editForm").get(0);
		$("#editableTable").subscribe("editTopic", function(event, ui) {
			clearErrorMessages($editForm);
			clearErrorLabels($editForm);
			var row = $("#editableTable tr.selected");
			if (row.length > 0) {
				var rowData = $("#editableTable").DataTable().row(row).data();
				$.each(rowData, function(name, value) {
					$("[name='" + name + "']").val(value);
				});
			}
		});
		$("#editableTable").subscribe("createTopic", function(event, ui) {
			clearErrorLabels($editForm);
			clearErrorMessages($editForm);
			$editForm.reset();
			$("#idUsers").val("");
		});
		$("#editableTable").subscribe("deleteTopic", function(event, ui) {
			var row = $("#editableTable tr.selected");
			if (row.length > 0) {
				var rowData = $("#editableTable").DataTable().row(row).data();
				$("input[name='id']").val(rowData.id);
			}
		});
		$("#editableTable")
				.subscribe(
						"rowDeletedErrorTopic",
						function(event, ui) {
							if (typeof event.originalEvent.request.responseJSON !== 'undefined'
									&& typeof event.originalEvent.request.responseJSON.actionErrors !== 'undefined') {
								alert(event.originalEvent.request.responseJSON.actionErrors[0]);
							} else {
								window.location = "/M9000Master/logout?customActionError=Session timed out";
							}

						});
		$("#editableTable")
				.subscribe(
						"rowSavedErrorTopic",
						function(event, ui) {
							if (typeof event.originalEvent.request.responseJSON !== 'undefined'
									&& typeof event.originalEvent.request.responseJSON.actionErrors !== 'undefined') {
								alert(event.originalEvent.request.responseJSON.actionErrors[0]);
							} else {
								window.location = "/M9000Master/logout?customActionError=Session timed out";
							}
						});

		$.subscribe('rowDeletedTopic', function(event, data) {
			$("#idUsers").val("");
		});
	});
</script>

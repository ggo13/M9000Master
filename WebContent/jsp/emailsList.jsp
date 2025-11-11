<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags" %>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>

<script>
    /*Destroy previous instances of jQuery Dialogs  */
	$(".ui-dialog-content").each(function(i,elt){
		if ($(elt).dialog){
			  $(this).dialog('destroy').remove();		  
		}
	});
</script>
<s:url var="remoteurl" action="emailsList-json" />
<sjdt:datatables id="emailTable" datatablesTheme="jqueryui"
	ajaxReloadTopics="emailSavedTopic,emailDeletedTopic" pageTopics="testTopic"
	buttons="[
{extend:'selectedSingle',text:'Edit',action:function(){$(this).publish('editEmailTopic');}},
{text:'Create',action:function(){$(this).publish('createEmailTopic');}},
{extend:'selectedSingle',text:'Delete',action:function(){$(this).publish('deleteEmailTopic');}}
]"
	dom="Blfrtip" ajax="{url:'%{remoteurl}',dataSrc:'lstEmailAddresses'}"
	select="'single'"
	columns="[
			{data:'firstName',title:'First Name', className: 'dt-body-center'},
            {data:'lastName',title:'Last Name', className: 'dt-body-center'},
            {data:'emailAddress',title:'Email-Id', className: 'dt-body-center'}
]"
	responsive="true" style="width:100%;">
	<caption class="ui-widget-header">Email Subscribers List</caption>
	<thead>
		<tr>
			<th>First Name</th>
			<th>Last Name</th>
			<th>Email-Id</th>
		</tr>
	</thead>
</sjdt:datatables>
<sj:dialog id="editEmailDialog" autoOpen="false" title="Edit Customer"
	openTopics="editEmailTopic,createEmailTopic" closeTopics="emailSavedTopic">
	<s:form id="editEmailForm" theme="xhtml" action="saveEmailDetails">
		<s:hidden id="idEmail" name="emailAddressDTO.id"/>
		<s:textfield id="firstName" name="emailAddressDTO.firstName" label="First Name" />
		<s:textfield id="lastName" name="emailAddressDTO.lastName" label="Last Name" />
		<s:textfield id="emailAddress" name="emailAddressDTO.emailAddress" label="Email Address" />
		<sj:submit value="Submit" button="true" buttonText="Submit"
			validate="true" dataType="json" targets="x"
			onSuccessTopics="emailSavedTopic" onErrorTopics="emailSavedErrorTopic" />
	</s:form>
</sj:dialog>
<sj:dialog id="deleteEmailDialog" autoOpen="false" title="Delete"
	openTopics="deleteEmailTopic" closeTopics="emailDeletedTopic" modal="true">
	<s:form id="deleteForm" theme="simple" action="deleteEmail">
		<s:hidden id="idEmail" name="emailAddressDTO.id" />
        Delete the row ?
        <div class="type-button">
			<sj:submit value="Confirm" button="true" targets="x" dataType="json"
				buttonText="Confirm" onSuccessTopics="emailDeletedTopic"
				onErrorTopics="rowDeletedErrorTopic" />
			<sj:a onClickTopics="emailDeletedTopic" button="true">Cancel</sj:a>
		</div>
	</s:form>
</sj:dialog>


<script>
	var $editEmailForm = null;
	$(function() {
		$editEmailForm = $("#editEmailForm").get(0);
		$("#emailTable").subscribe("editEmailTopic", function(event, ui) {
			clearErrorMessages($editEmailForm);
			clearErrorLabels($editEmailForm);
			var row = $("#emailTable tr.selected");
			if (row.length > 0) {
				var rowData = $("#emailTable").DataTable().row(row).data();
				$.each(rowData, function(name, value) {
					console.log("Name "+name);
					$("[name='emailAddressDTO." + name + "']").val(value);
				});
			}
		});
		$("#emailTable").subscribe("createEmailTopic", function(event, ui) {
			clearErrorLabels($editEmailForm);
			clearErrorMessages($editEmailForm);
			$editEmailForm.reset();
			$("#idEmail").val("");
		});
		$("#emailTable").subscribe("deleteEmailTopic", function(event, ui) {
			var row = $("#emailTable tr.selected");
			if (row.length > 0) {
				var rowData = $("#emailTable").DataTable().row(row).data();
				$("input[name='emailAddressDTO.id']").val(rowData.id);
			}
		});
		$("#emailTable")
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
		$("#emailTable")
				.subscribe(
						"emailSavedErrorTopic",
						function(event, ui) {
							if (typeof event.originalEvent.request.responseJSON !== 'undefined'
									&& typeof event.originalEvent.request.responseJSON.actionErrors !== 'undefined') {
								alert(event.originalEvent.request.responseJSON.actionErrors[0]);
							} else {
								window.location = "/M9000Master/logout?customActionError=Session timed out";
							}
						});

		$.subscribe('emailDeletedTopic', function(event, data) {
			$("#idEmail").val("");
		});
	});
</script>

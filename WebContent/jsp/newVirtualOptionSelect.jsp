<%@ taglib prefix="s" uri="/struts-tags"%>
<script  type="text/javascript">
$(document).ready(function(){
var existDecisionLogic = $('#virtualLogic').val();
console.log("Existing logic "+existDecisionLogic);
if (existDecisionLogic != '')
{
        $('#virtualScale').prop("disabled", false);
        $('#btnDelete').prop("disabled", false);
        $('#btnClear').prop("disabled", false);
}
else
{
        $('#virtualScale').prop("disabled", true);
        $('#btnDelete').prop("disabled", true);
        $('#btnClear').prop("disabled", true);
}
});
</script>
<table class="wwFormTable">
<tr >
        <td colspan="3"><s:label theme="simple" cssClass="titleLabel"
                        value="Virtual Channel Logic"></s:label>
                        <s:textfield theme="simple" cssStyle="width:100%" readonly="true"
                                id="virtualLogic" name="virtualLogic" onclick="alert('Please use bottom section to add virtual logic');"
                                value="%{virtualLogic}"
                                label="Virtual Channel Logic">
                        </s:textfield>
         </td>
      </tr>
	<tr >
	<s:if test="%{lstDfrsAnalogChannels.size > 0}">
		<td colspan="2" > 
			<s:label theme="simple" cssClass="titleLabel"
			value="Available Channels List"></s:label> 

                        <s:select
                        cssStyle="font-family:courier, courier new, serif;border-style: solid solid solid solid;font-weight:normal;"
                        theme="simple" id="analogs" name="analogs" size="10"
                        list="lstDfrsAnalogChannels" listKey="displayChannel" onclick="javascript:enableOperations();return false;"
                        listValue="displayName" 
                        ></s:select>

	 	</td>
	 	 
	 	         <td>
	 	         	<table class="inlineTable">
	 	         	<tr><td>
					<s:submit theme="simple" cssClass="submit" name="btnAdd"
						value="Add" cssStyle="font-weight:normal;width:100%" disabled="true"
						onclick="javascript:populateDecisionLogic(this.value);return false;" />
				</td></tr>
				<tr><td>
					<s:submit theme="simple" cssClass="submit" name="btnSub"
						value="Subtract" cssStyle="font-weight:normal;width:100%" disabled="true"
						onclick="javascript:populateDecisionLogic(this.value);return false;" />
				</td></tr>
				<tr><td>
					<s:submit theme="simple" cssClass="submit" name="btnDelete"
						value="Delete" cssStyle="font-weight:normal;width:100%" disabled="true"
						onclick="javascript:populateDecisionLogic(this.value);return false;" />
				</td></tr>
				<tr><td>
					<s:submit theme="simple" cssClass="submit" name="btnClear"
						value="Clear" cssStyle="font-weight:normal;width:100%" disabled="true"
						onclick="javascript:populateDecisionLogic(this.value);return false;" />
				</td></tr>
				</table>
			</td>
</s:if>
<s:else>
	<td>
		<div style="color:RED">
			There are no channels found matching the filter criteria
		</div>
	</td>
</s:else>
	 </tr>

</table>


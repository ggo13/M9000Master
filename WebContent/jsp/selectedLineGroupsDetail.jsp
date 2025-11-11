<%@ taglib prefix="s" uri="/struts-tags"%>
selectedLineGroupIndex
<s:property value="%{selectedLineGroupIndex}" />
<s:select list="lstLineGroups[selectedLineGroupIndex].inputChannels"
	id="lineGroupDetails" name="lineGroupDetails" multiple="true" />

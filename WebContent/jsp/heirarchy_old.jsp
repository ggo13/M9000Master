<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
    <title>Hierarchy Visualization</title>
    <!-- Add your CSS styling here to format the hierarchy visualization -->
    <style>
        /* Style for the hierarchy elements */
        .node {
            border: 1px solid #ccc;
            padding: 5px;
            margin: 5px;
            cursor: move;
        }
        .editable {
            border: 1px solid #ccc;
            padding: 3px;
            margin: 3px;
        }
    </style>
</head>
<body>
    <h1>Hierarchy Visualization</h1>

    <div id="hierarchy">
        <!-- Recursive method to display the hierarchy -->
        <s:iterator value="hierarchy">
            <s:set var="node" value="top" />
            <s:iterator value="children">
                <s:set var="node" value="top" />
                <div class="node" id="node-<s:property value='name'/>" ondblclick="enableEdit(this)">
                    <span class="editable" style="display:none;" onblur="disableEdit(this)" onkeypress="updateName(event, this)"><s:property value="name" /></span>
                    <span class="display"><s:property value="name" /></span>
                </div>
                <s:if test="node.children.size() > 0">
                    <div id="children-<s:property value='name'/>">
                        <s:include value="/WEB-INF/jsp/hierarchy.jsp" />
                    </div>
                </s:if>
            </s:iterator>
        </s:iterator>
    </div>

    <script>
        function enableEdit(element) {
            var editable = $(element).children(".editable");
            var display = $(element).children(".display");
            editable.val(display.text());
            display.hide();
            editable.show();
            editable.focus();
        }

        function disableEdit(element) {
            var editable = $(element);
            var display = editable.siblings(".display");
            display.text(editable.val());
            display.show();
            editable.hide();

            // Update the hierarchy data and send to the backend (you need to implement this part)
            // For simplicity, we'll assume the hierarchy data is in a global variable named "hierarchyData"
            var nodeName = display.text();
            var newNodeName = editable.val();

            // Update the hierarchyData to reflect the change
            updateHierarchyData(nodeName, newNodeName);
        }

        function updateName(event, element) {
            if (event.keyCode === 13) { // Enter key pressed
                disableEdit(element);
            }
        }

        // Function to update the hierarchy data (you need to implement this part)
        function updateHierarchyData(nodeName, newNodeName) {
            // Implement the logic to update the hierarchy data
            // You can use Ajax to send the updated data to the backend and save it.
            // For simplicity, we'll just log the update here.
            console.log("Updated node name from '" + nodeName + "' to '" + newNodeName + "'");
        }

        $(document).ready(function() {
            // Enable drag-and-drop for nodes with class "node"
            $(".node").draggable({
                helper: "clone",
                opacity: 0.7,
                revert: "invalid",
            });

            // Enable drop functionality for divs with id starting with "children-"
            $("div[id^='children-']").droppable({
                tolerance: "pointer",
                drop: function(event, ui) {
                    var draggableNode = ui.helper[0];
                    var targetNodeId = $(this).attr("id").substring(9); // Remove "children-" from the id
                    var targetNode = document.getElementById("node-" + targetNodeId);

                    // Append the dragged node to the target node's children
                    $(draggableNode).appendTo(targetNode);

                    // Update the hierarchy data and send to the backend (you need to implement this part)
                    // For simplicity, we'll assume the hierarchy data is in a global variable named "hierarchyData"
                    var draggedNodeName = $(draggableNode).find(".display").text();
                    var targetNodeName = $(targetNode).find(".display").text();

                    // Update the hierarchyData to reflect the change
                    updateHierarchyData(draggedNodeName, targetNodeName);
                }
            });
        });
    </script>
</body>
</html>

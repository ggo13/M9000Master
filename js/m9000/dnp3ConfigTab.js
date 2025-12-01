$(document).ready(function () {
    // Show/hide TCP/Serial fields when transport method changes
    $('.outstation-form-body').on('change', 'input[name$=".transportMethod"]', function () {
        var transportVal = $(this).val();
        var $container = $(this).closest('.outstation-form-body');

        if (transportVal === 'TCP/IP') {
            $container.find('.tcp-port-container').show();
            $container.find('.serial-port-container').hide();
        } else if (transportVal === 'Serial') {
            $container.find('.tcp-port-container').hide();
            $container.find('.serial-port-container').show();
        }
    });

    // Trigger the change event on page load to set correct initial visibility
    $('.outstation-form-body input[name$=".transportMethod"]:checked').trigger('change');
    
    function reindexFormRows() {
        $('.form-input-row').each(function (index) {
            // Don't change the value of the index field — allow user input
            var $indexInput = $(this).find('input.dnp3-index-value');
            var userIndexValue = $indexInput.val();

            // Set name based on user-typed index value
            $indexInput.attr('name', 'lstDnp3Configurations[' + userIndexValue + '].dnpIndex');

            // Update select field names to match the user-defined index
            $(this).find('select').each(function () {
                var $select = $(this);

                if ($select.attr('name').includes('.dnpChannel')) {
                    $select.attr('name', 'lstDnp3Configurations[' + userIndexValue + '].dnpChannel');
                } else if ($select.attr('name').includes('.sourceType')) {
                    $select.attr('name', 'lstDnp3Configurations[' + userIndexValue + '].sourceType');
                }
            });
        });
    }

    // Add row logic
    $('#add-row-btn').on('click', function () {
        var $newRow = $('.form-input-row:first').clone();

        // Reset input/select values
        $newRow.find('input.dnp3-index-value').val('');
        $newRow.find('select').each(function () {
            this.selectedIndex = 0;
        });

        $('.form-body').append($newRow);
        reindexFormRows();
    });

    // Delete row logic (event delegation for dynamic rows)
    $('.form-body').on('click', '.delete-row-btn', function () {
        if ($('.form-input-row').length > 1) {
            $(this).closest('.form-input-row').remove();
            reindexFormRows();
        } else {
            alert('At least one configuration row is required.');
        }
    });

    function validateUniqueIndices() {
        var indices = new Set();
        var isValid = true;

        $('.form-input-row').each(function () {
            var val = $(this).find('input.dnp3-index-value').val();
            if (val === '' || indices.has(val)) {
                isValid = false;
                $(this).find('input.dnp3-index-value').css('border', '2px solid red');
            } else {
                indices.add(val);
                $(this).find('input.dnp3-index-value').css('border', '');
            }
        });

        return isValid;
    }

    // Call this function before form submit
    $('.save-button').on('click', function (e) {
        reindexFormRows(); // Update all name attributes
        if (!validateUniqueIndices()) {
            e.preventDefault(); // Cancel form submit if invalid
            return false;
        }
    });
});
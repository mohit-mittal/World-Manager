var running_proc_table1 = $('#running_proc_table1'); // This Table shows 180 GPUs of 60 gpus (First Table)
var running_proc_table2 = $('#running_proc_table2'); // This Table shows 192 GPUs of 24 gpus (Second Table)

function get_run_proc_Data() {
                var getButton = "get_run_proc_button";
            var reload = "get_run_proc_Data";
            main(getButton, running_proc_table1, running_proc_table2, reload);    //calls main(). It is defined in include.js

    $(".container-run-proc").empty();
}
get_run_proc_Data();

function display_run_proc(val) {
	return val;
}

function range_run_proc(val) {
        if (val == 0) {
            retval = "btn-default";
        } else
        if (val == 1) {
            retval = "btn-info";
        } else {
            retval = "btn-black";
        }
	return retval;
}

function get_run_proc_button(i, item) {
	
	var retval = create_button_instance(item,"gpu"+i,"run_proc",range_run_proc,display_run_proc);
    return retval;
}
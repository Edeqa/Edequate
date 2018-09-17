/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 9/14/18.
 */
function HomeHolder(main) {
    var self = this;
    var u = main.edequate;

    var STATUS_STOPPED = 0;
    var STATUS_RUNNING = 1;
    var STATUS_WAITING = 2;

    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "home";
    this.title = "Dashboard";
    this.menu = "Dashboard";
    this.icon = "home";
    this.priority = 10;
    this.scrollTop = 0;
    var div;
    var dialogRestart;
    var dialogStop;
    var tableSummary;
    var previousStatus = null;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option"
        }, div);
        tableSummary.statusItem = tableSummary.add({
            cells: [
                { className: "th", innerHTML: "Server status" },
                { className: "option", innerHTML: "n/a" },
                { className: "option", children: [
                        u.create(HTML.BUTTON, {
                            className: "icon notranslate button-inline",
                            innerHTML: "refresh",
                            title: "Restart",
                            onclick: askRestart
                        }),
                        u.create(HTML.BUTTON, {
                            className: "icon notranslate button-inline",
                            innerHTML: "stop",
                            title: "Stop",
                            onclick: askStop
                        })
                ] }
            ]
        });
        tableSummary.uptimeItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Uptime" },
                { className:"option", innerHTML: 0, title: "Click for update", onclick: updateUptime }
            ]
        });

        previousStatus = null;
        updateUptime();
        tableSummary.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === tableSummary) {
                clearInterval(tableSummary.taskUpdateUptime);
            }
        }, {passive: true});
    };

    function updateStatus(status) {
        if(previousStatus !== status) {
            if(status === STATUS_STOPPED) {
                tableSummary.statusItem.childNodes[1].innerHTML = "stopped";
                tableSummary.statusItem.childNodes[1].classList.add("warning");
                tableSummary.statusItem.childNodes[2].classList.add("hidden");
            } else if(status === STATUS_RUNNING) {
                tableSummary.statusItem.childNodes[1].innerHTML = "running";
                tableSummary.statusItem.childNodes[1].classList.remove("warning");
                tableSummary.statusItem.childNodes[2].classList.remove("hidden");
            } else if(status === STATUS_WAITING) {
                tableSummary.statusItem.childNodes[1].innerHTML = "waiting";
                tableSummary.statusItem.childNodes[2].classList.add("hidden");
            }
            previousStatus = status;
        }
    }

    function updateUptime() {
        u.getJSON("/rest/uptime").then(function(json){
            tableSummary.uptimeItem.childNodes[1].innerHTML = json.message;
            updateStatus(STATUS_RUNNING);
        }).catch(function(e,x){
            console.error(e,x);
            tableSummary.uptimeItem.childNodes[1].innerHTML = "n/a";
            updateStatus(STATUS_STOPPED);
            setTimeout(updateUptime, 1000);
        });
    }

    function askRestart() {
        dialogRestart = dialogRestart || u.dialog({
            title: "Restart server",
            modal: true,
            items: [
                { innerHTML: "Do you want to restart the server? It will take some time." }
            ],
            positive: {
                label: "Yes",
                onclick: function() {
                    console.log("restart");
                    u.toast.show("Server is restarting");
                    tableSummary.uptimeItem.childNodes[1].innerHTML = "n/a";
                    updateStatus(STATUS_WAITING);
                    u.getJSON("/admin/rest/restart", {restart:true}).then(function(json){
                        console.log("RESULT", json);
                        setTimeout(updateUptime, 1000);
                    }).catch(function(e,x){
                        console.error(e,x);
                        setTimeout(updateUptime, 1000);
                    });
                }
            },
            negative: {
                label: "No"
            }
        });
        dialogRestart.open();
    }

    function askStop() {
        dialogStop = dialogStop || u.dialog({
            title: "Stop server",
            modal: true,
            items: [
                {innerHTML: "Do you want to stop the server?"}
            ],
            positive: {
                label: "Yes",
                onclick: function() {
                    console.log("stop");
                    u.toast.show("Server is stopping");
                    tableSummary.uptimeItem.childNodes[1].innerHTML = "n/a";
                    updateStatus(STATUS_WAITING);
                    u.getJSON("/admin/rest/restart", {stop:true}).then(function(json){
                        console.log("RESULT", json);
                        setTimeout(updateUptime, 1000);
                    }).catch(function(e,x){
                        console.error(e,x);
                        setTimeout(updateUptime, 1000);
                    });
                }
            },
            negative: {
                label: "No"
            }
        });
        dialogStop.open();
    }
}

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
    var delayStart;
    var buttonRestart;
    var buttonStop;
    var taskDelayUpdate;
    var runningTime;
    var taskRunningTimeUpdate;
    var updateInterval = 1000;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, "Summary", div);

        buttonRestart = u.create(HTML.BUTTON, {
            className: "icon notranslate button-inline",
            innerHTML: "refresh",
            title: "Restart server",
            onclick: askRestart
        }, titleNode);

        buttonStop = u.create(HTML.BUTTON, {
            className: "icon notranslate button-inline",
            innerHTML: "stop server",
            title: "Stop",
            onclick: askStop
        }, titleNode);

        tableSummary = u.table({
            className: "option"
        }, div);
        tableSummary.statusItem = tableSummary.add({
            cells: [
                { className: "th", innerHTML: "Server status" },
                { className: "option", innerHTML: "n/a" }/*,
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
                ] }*/
            ]
        });
        tableSummary.uptimeItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Uptime" },
                { className:"option", innerHTML: "n/a", title: "Click for update", onclick: updateUptime }
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
                buttonRestart.classList.add("hidden");
                buttonStop.classList.add("hidden");
                if(previousStatus === STATUS_WAITING) {
                    u.toast.error("Server has stopped");
                }
            } else if(status === STATUS_RUNNING) {
                tableSummary.statusItem.childNodes[1].innerHTML = "running";
                tableSummary.statusItem.childNodes[1].classList.remove("warning");
                buttonRestart.classList.remove("hidden");
                buttonStop.classList.remove("hidden");
                if(previousStatus === STATUS_WAITING) {
                    u.toast.error("Server has started");
                }
            } else if(status === STATUS_WAITING) {
                tableSummary.statusItem.childNodes[1].innerHTML = "waiting";
                buttonRestart.classList.add("hidden");
                buttonStop.classList.add("hidden");
            }
            previousStatus = status;
        }
    }

    function formatRunningTime(millis) {
        var seconds = Math.floor(millis/1000);
        millis -= seconds*1000;

        var minutes = Math.floor(seconds/60);
        seconds -= minutes*60;

        var hours = Math.floor(minutes/60);
        minutes -= hours*60;

        var days = Math.floor(hours/24);
        hours -= days*24;

        return "%d:%02d:%02d:%02d".sprintf(days, hours, minutes, seconds);
    }

    function updateUptime() {
        clearInterval(taskRunningTimeUpdate);
        u.getJSON("/rest/uptime", null, 1000).then(function(json){
            delayStart = 0;
            runningTime = json.extra;
            function runningTimeUpdate() {
                tableSummary.uptimeItem.childNodes[1].innerHTML = formatRunningTime(runningTime);
                runningTime += 1000;
            }
            runningTimeUpdate();
            clearInterval(taskDelayUpdate);
            taskRunningTimeUpdate = setInterval(runningTimeUpdate, 1000);
            updateStatus(STATUS_RUNNING);
        }).catch(function(e,x){
            console.error(e,x);
            delayStart = 0;
            updateStatus(STATUS_STOPPED);
            setTimeout(updateUptime, updateInterval);
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
                    u.toast.show("Server is restarting");
                    tableSummary.uptimeItem.childNodes[1].innerHTML = "n/a";
                    updateStatus(STATUS_WAITING);
                    delayStart = new Date().getTime();

                    function restarting() {
                        if(delayStart) {
                            var delay = (new Date().getTime() - delayStart) / 1000;
                            tableSummary.uptimeItem.childNodes[1].innerHTML = "restarting " + delay.toFixed(0) + " s...";
                        } else {
                            tableSummary.uptimeItem.childNodes[1].innerHTML = "n/a";
                        }
                    }
                    clearInterval(taskRunningTimeUpdate);
                    restarting();
                    taskDelayUpdate = setInterval(restarting, 1000);
                    updateInterval = 1000;
                    u.getJSON("/admin/rest/restart", {restart:true}, 1000).then(function(json){
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
                {innerHTML: "Do you want to stop the server? Then you should be able to start it directly."}
            ],
            positive: {
                label: "Yes",
                onclick: function() {
                    u.toast.show("Server is stopping");
                    tableSummary.uptimeItem.childNodes[1].innerHTML = "n/a";
                    clearInterval(taskRunningTimeUpdate);
                    updateStatus(STATUS_WAITING);
                    updateInterval = 10000;
                    u.getJSON("/admin/rest/restart", {stop:true}, 1000).then(function(json){
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

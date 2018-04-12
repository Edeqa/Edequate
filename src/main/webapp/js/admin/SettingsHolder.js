/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 4/5/18.
 */
function SettingsHolder(main) {
    var self = this;
    var u = main.edequate;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "settings";
    this.title = "Settings";
    this.menu = "Settings";
    this.icon = "settings";
    this.priority = -10;

    var div;
    var dialogEmailSettings;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, "Settings", div);

        u.get("/admin/rest/data/settings").then(function(result){
            console.log(result);
            var json;
            if(result.responseText) {
                json = JSON.parse(result.responseText);
            } else {
                json = {};
            }
            populateSettings(json);
            main.eventBus.fire("settings", div);
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    function populateSettings(json) {
        populateMailSettings(json.mail || {});
    }

    function populateMailSettings(json) {

        dialogEmailSettings = dialogEmailSettings || u.dialog({
            title: "SMTP settings",
            items: [
                {type: HTML.INPUT, label: "SMTP server"},
                {type: HTML.INPUT, label: "SMTP port"},
                {type: HTML.INPUT, label: "SMTP login"},
                {type: HTML.INPUT, label: "SMTP password"},
                {type: HTML.INPUT, label: "Reply name"},
                {type: HTML.INPUT, label: "Reply e-mail"}
            ],
            positive: {
                label: u.lang.ok,
                dismiss: false,
                onclick: function() {
                    console.log("save");
                }
            },
            neutral: {
                label: "Test",
                dismiss: false,
                onclick: function() {
                    console.log("test")
                }
            },
            negative: {
                label: u.lang.cancel,
                onclick: function() {
                    console.log("cancel")
                }
            }
        });

        u.create(HTML.DIV, {className: "settings-item"}, div)
            .place(HTML.DIV, {className: "settings-item-label", innerHTML:"E-mail settings"})
            .place(HTML.DIV, {className: "settings-item-input", children: [
                    u.create(HTML.DIV, json.smtp_server ? json.smtp_server + (json.smtp_port ? ":" + json.smtp_port : "") + (json.smtp_login ? "/" + json.smtp_login : "") : "Not defined"),
                    u.create(HTML.BUTTON, {
                        className: "icon settings-item-button",
                        innerHTML: "edit",
                        onclick: function () {
                            dialogEmailSettings.open()
                        }
                    })
                ]
            });

        var serverNode = dialogEmailSettings.items[0];
        var portNode = dialogEmailSettings.items[1];
        var loginNode = dialogEmailSettings.items[2];
        var passwordNode = dialogEmailSettings.items[3];
        var nameNode = dialogEmailSettings.items[4];
        var emailNode = dialogEmailSettings.items[5];

        serverNode.value = json.smtp_server || "";
        portNode.value = json.smtp_port || "";
        loginNode.value = json.smtp_login || "";
        passwordNode.value = json.smtp_password || "";
        nameNode.value = json.reply_name || "";
        emailNode.value = json.reply_email || "";

    }
}

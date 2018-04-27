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
    var dialogTest;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, "Settings", div);

        u.get("/admin/rest/data/settings").then(function(result){
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
                {type: HTML.INPUT, labelClassName: "required", label: "SMTP server"},
                {type: HTML.INPUT, labelClassName: "required", label: "SMTP port"},
                {type: HTML.INPUT, labelClassName: "required", label: "SMTP login"},
                {type: HTML.PASSWORD, labelClassName: "required", label: "SMTP password"},
                {type: HTML.INPUT, label: "Reply name"},
                {type: HTML.INPUT, label: "Reply e-mail"}
            ],
            positive: {
                label: u.lang.ok,
                dismiss: false,
                onclick: function() {
                    console.log("save");
                    var options = {
                        action: "smtp_save",
                        smtp_server: serverNode.value,
                        smtp_port: portNode.value,
                        smtp_login: loginNode.value,
                        smtp_password: passwordNode.value,
                        reply_name: nameNode.value,
                        reply_email: emailNode.value
                    };
                    u.progress.show("Saving SMTP settings...");
                    u.post("/admin/rest/settings", options).then(function(json) {
                        console.log(json);
                        u.progress.hide();
                        dialogEmailSettings.close();
                        main.turn("settings");
                        u.toast.show("Settings has been saved");
                    }).catch(function (code, xhr) {
                        console.error(code);
                        u.progress.hide();
                        try{
                            var json = JSON.parse(xhr.response);
                            u.toast.error("Saving failed: " + json.message);
                        } catch(e) {
                            console.error(code);
                            u.toast.error("Saving failed: error " + error);
                        }

                    });
                }
            },
            neutral: {
                label: "Test",
                dismiss: false,
                onclick: function() {
                    console.log("test");

                    dialogTest = dialogTest || u.dialog({
                        title: "Test to e-mail",
                        items: [
                            {type: HTML.INPUT, labelClassName: "required", label: "Enter target e-mail"},
                        ],
                        positive: {
                            label: u.lang.ok,
                            onclick: function() {
                                var options = {
                                    action: "smtp_test",
                                    smtp_server: serverNode.value,
                                    smtp_port: portNode.value,
                                    smtp_login: loginNode.value,
                                    smtp_password: passwordNode.value,
                                    reply_name: nameNode.value,
                                    reply_email: emailNode.value,
                                    target_email: dialogTest.items[0].value
                                };
                                u.progress.show("Sending test e-mail...");
                                u.post("/admin/rest/settings", options).then(function(json) {
                                    u.progress.hide();
                                    u.toast.show("E-mail sent, check your inbox");
                                }).catch(function (code, xhr) {
                                    console.error(code);
                                    u.progress.hide();
                                    try{
                                        var json = JSON.parse(xhr.response);
                                        u.toast.error("E-mail sent failed: " + json.message);
                                    } catch(e) {
                                        console.error(code);
                                        u.toast.error("E-mail sent failed: error " + error);
                                    }

                                });
                            }
                        },
                        negative: {
                            label: u.lang.cancel,
                            onclick: function() {
                                console.log("cancel")
                            }
                        }
                    });
                    dialogTest.open();
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
            .place(HTML.DIV, {className: "settings-item-label" + (!json.smtp_server || !json.smtp_port || !json.smtp_login || !json.smtp_password || !json.reply_name || !json.reply_email ? " question" : ""), innerHTML:"SMTP settings"})
            .place(HTML.DIV, {className: "settings-item-input", children: [
                    u.create(HTML.DIV, json.smtp_server ? json.smtp_server + (json.smtp_port ? ":" + json.smtp_port : "") + (json.smtp_login ? "/" + json.smtp_login : "") : "Not defined"),
                    u.create(HTML.BUTTON, {
                        className: "icon button-inline",
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

/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 3/21/18.
 */
function AdminHolder(main) {
    var self = this;
    var u = main.edequate;

    this.type = "admin";
    this.title = "Admins";
    this.preventHistory = true;
    var div;
    var dialog;
    var alert;


    /** @namespace json.message.login */
    this.start = function() {
        div = main.content;
        u.getJSON("/admin/rest/data/admins", {mode:"current"}).then(function(json){
            if(json && json.message) {
                var security = json.message.security || "missing";
                if(security !== "strong" && security !== "medium") {
                    alert = alert || u.create(HTML.DIV, {className: "alert-box"})
                        .place(HTML.DIV, {innerHTML: "Your password is " + security + "."})
                        .place(HTML.BUTTON, {innerHTML:"Update", className:"dialog-button", onclick: function() {
                                main.turn("admin", ["password", json.message.login]);
                            }});
                    placeAlert();
                }
                main.drawer.headerSubtitle.setContent(u.create(HTML.DIV, {
                    innerHTML: json.message.name || json.message.login,
                    title: "Login: " + json.message.login
                        + (json.message.name ? ", name: " + json.message.name : "")
                        + ", security: " + security
                        + (json.message.expiration ? ", expiration: " + new Date(json.message.expiration).toDateString() : "")

                })/*.place(HTML.BUTTON, {innerHTML:"edit", title:"Edit", className:"icon notranslate drawer-header-subtitle-icon security-" + (json.message.security || "missing").replace(/[\W]/g,"-"), onclick: function() {
                        main.turn("admin", ["edit", json.message.login])
                    }})*/);
            }
        }).catch(function(e,x){
            console.error(e,x);
        });
        main.drawer.headerSubtitle.innerHTML = window.data.user || "admin";
    };

    this.resume = function(action,id) {
        switch(action) {
            case "edit":
            case "password":
                if(!id) {
                    main.turn("admins");
                    return;
                }
                u.getJSON("/admin/rest/data/admins", {mode:"select", login:id}).then(function(json){
                    console.log(json.message);
                    editAdmin(json.message, {mode:"Edit admin", action:action});
                }).catch(function(e,x){
                    console.error(e,x);
                });
                break;
            case "add":
                editAdmin({}, {mode:"Add admin", action:action});
                break;
            default:
                main.turn("admins");
                return;
        }
        window.history.pushState({}, null, "/admin/admin/" + action + (id ? "/" + id : ""));
    };

    function editAdmin(admin, options) {
        try {
            dialog = dialog || u.dialog({
                title: "Admin",
                className: "admin-edit-dialog",
                items: [
                    {type: HTML.INPUT, label: "Login", required: true, tabindex: 1 },
                    {type: HTML.PASSWORD, label: "Password", tabindex: 2 },
                    {type: HTML.PASSWORD, label: "Confirm password", tabindex: 3 },
                    {type: HTML.INPUT, label: "Name", tabindex: 4 },
                    {type: HTML.INPUT, label: "E-mail", tabindex: 5 },
                    {type: HTML.DATETIME_LOCAL, label: "Expiration", tabindex: 6 },
                    {type: HTML.SELECT, label: "Roles", tabindex: 7, values: {
                            "administrator": "administrator"
                        }
                    }
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    dismiss: false,
                    onclick: function () {
                        if(!loginNode.value) {
                            u.toast.error("Login not defined");
                            loginNode.focus();
                            return;
                        }
                        if(!passwordNode.value && dialog.initialOptions.security === "missing") {
                            u.toast.error("Password not defined");
                            passwordNode.focus();
                            return;
                        }
                        if(passwordNode.value.length > 0 && passwordNode.value.length < 6) {
                            u.toast.error("Password too short");
                            passwordNode.focus();
                            return;
                        }
                        if(passwordNode.value !== confirmPasswordNode.value) {
                            u.toast.error("Password not confirmed");
                            confirmPasswordNode.focus();
                            return;
                        }

                        u.progress.show("Saving...");
                        var resultOptions = {
                            login: loginNode.value,
                            password: passwordNode.value,
                            name: nameNode.value,
                            email: emailNode.value,
                            expiration: expirationNode.value ? new Date(expirationNode.value).getTime() : 0,
                            roles: rolesNode.value,
                            add: dialog.optionals.action === "add"
                        };
                        u.post("/admin/rest/data/admins", {initial: dialog.initialOptions, mode:"save", admin: resultOptions}).then(function(result){
                            u.progress.hide();
                            u.toast.show("Admin saved");
                            dialog.close();
                            main.turn("admins");
                        }).catch(function (code, reason) {
                            u.progress.hide();
                            var json = JSON.parse(reason.response);
                            var message = json.message || (reason && reason.statusText);
                            u.toast.error(message || "Error saving admin");
                        });
                    }
                },
                negative: {
                    label: u.create(HTML.SPAN, "Cancel"),
                    onclick: function () {
                        main.turn("admins");
                    }
                }
            }, div.parentNode);
            // dialog.setTitle(options.mode);
            var loginNode = dialog.items[0];
            var passwordNode = dialog.items[1];
            var confirmPasswordNode = dialog.items[2];
            var nameNode = dialog.items[3];
            var emailNode = dialog.items[4];
            var expirationNode = dialog.items[5];
            var rolesNode = dialog.items[6];


            loginNode.value = admin.login || "";
            // loginNode.disabled = options.action !== "add";

            passwordNode.value = "";
            passwordNode.placeholder = "";
            confirmPasswordNode.placeholder = "";
            if(admin.security !== "missing") {
                passwordNode.placeholder = "Defined, " + admin.security;
                confirmPasswordNode.placeholder = "Defined";
            }

            confirmPasswordNode.value = "";
            nameNode.value = admin.name || "";
            emailNode.value = admin.email || "";
            expirationNode.value = admin.expiration || "";
            rolesNode.value = admin.roles || "administrator";

            dialog.initialOptions = admin;
            dialog.optionals = options;

            dialog.setTitle(options.mode);
            dialog.open();

            if(options.action === "password") {
                dialog.focus();
                passwordNode.focus();
            }
        } catch(e) {
            console.error(e);
            // main.turn("admins");
        }
    }

    this.onEvent = function(event) {
        switch(event) {
            case "pages_done":
            case "turn":
                placeAlert();
                break;
        }
    };

    function placeAlert() {
        if(alert) {
            div.insertBefore(alert, div.firstChild);
        }
    }

}

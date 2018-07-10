/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 7/10/18.
 */
function IgnoredHolder(main) {
    var self = this;
    var u = main.edequate;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "ignored";
    this.title = "Ignored";
    this.menu = "Ignored";
    this.icon = "group";
    this.scrollTop = 0;
    var div;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, self.title, div);

        u.create(HTML.BUTTON, { className:"icon notranslate button-inline", innerHTML: "add", title:"Add path", onclick: function(){
                self.scrollTop = main.content.scrollTop;
                main.turn("admin", ["add"]);
            }}, titleNode);
        u.create(HTML.BUTTON, { className:"icon notranslate button-inline", innerHTML: "security", title:"Roles", onclick: function(){
                self.scrollTop = main.content.scrollTop;
                main.turn("roles");
            }}, titleNode);

        var tableUsers = u.table({
            id: "groups",
            className: "admins",
            caption: {
                items: [
                    { label: "Login" },
                    { label: "Name" },
                    { label: "E-mail" },
                    { label: "Realm", selectable: true },
                    { label: "Security", selectable: true },
                    { label: "Expiration" },
                    { label: "Roles", selectable: true }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.getJSON("/admin/rest/data/admins", {mode:"list"}).then(function(json){
            for(var i in json.message) {
                var user = json.message[i];

                tableUsers.add({
                    id: user.login,
                    className: "highlight" + " security-" + (user.security || "missing").replace(/[\W]/g,"-"),
                    onclick: function(){
                        main.turn("admin", ["edit", this.id]);
                        return false;
                    },
                    cells: [
                        { innerHTML: u.clear(user.login) },
                        { innerHTML: u.clear(user.name) || "&#150" },
                        { innerHTML: u.clear(user.email) || "&#150" },
                        { innerHTML: u.clear(user.realm) || "&#150"},
                        { innerHTML: u.clear(user.security) },
                        { innerHTML: user.expiration ? new Date(user.expiration).toLocaleString() : "&#150;"},
                        { innerHTML: u.clear(user.roles) }
                    ]
                });
            }
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case "settings":
                object.appendChild(u.create(HTML.DIV, "Admins settings"));
                break;
        }
        return true;
    }
}

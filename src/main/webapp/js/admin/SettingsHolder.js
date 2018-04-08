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
    var dialogConfirm;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, "Settings", div);

        var settingsNode = u.create(HTML.DIV, {}, div);

        main.eventBus.fire("settings", settingsNode);

    }

}

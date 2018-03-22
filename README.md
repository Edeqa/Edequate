# Edequate


Site building framework.

Includes javascript DOM and interface routines.

## How to use

### Including to the project

First, make a clone of Edequate into your project:

    git clone https://github.com/Edeqa/Edequate.git Edequate
    
register it into settings.gradle:

    include ':Edequate'

and into your build.gradle:

    dependencies {
        compile (project(':Edequate')) {
            exclude group: 'org.json', module: 'json'
            exclude group: 'com.google.guava', module: 'guava'
            exclude group: 'javax.servlet', module: 'javax.servlet-api'
        }
    }
    
    task updateChanged(type: Copy) {
        from "${project(':Edequate').projectDir}/src/main/webapp", "${projectDir}/src/main/webapp"
        into "${buildDir}/exploded-app"
        exclude "**/.idea/*"
    }
    
    war.dependsOn(':Edequate:war')
    
    war {
        with copySpec {
            from zipTree("${project(':Edequate').buildDir}/libs/Edequate.war")
            into("/")
            duplicatesStrategy DuplicatesStrategy.EXCLUDE
        }
    }

### Reorganize your project


### Set index.html

Set the `index.html` file as a head of your project, change it as you'd like:

```html
    <!--?xml version="1.0" encoding="UTF-8"?-->
    <!DOCTYPE html>
    <html>
    <head>
        <title>Edequate</title>
        <link rel="icon" href="/icons/favicon.ico">
        <style>
            @import url("/css/edequate.css");
            @import url("/css/edequate-horizontal.css");
        </style>
        <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
        <script async src="/js/Edequate.js"
            data-variable="u"
            data-callback="u.require('/js/main/Main.js', u).then(function(main){main.start({type:'main'})})"
            data-export-constants="true">
        </script>
    </head>
    <body>
    <div id="loading-dialog" class="modal shadow progress-dialog" tabindex="-1">
        <div class="dialog-items">
            <div class="dialog-item progress-dialog-circle"></div>
            <div class="dialog-item progress-dialog-title">Loading...</div>
            <div id="loading-dialog-progress" class="dialog-item progress-dialog-title"></div>
        </div>
    </div>
    <noscript>
        <link type="text/css" rel="stylesheet" href="/css/noscript.css">
        <div class="header">
            <img src="/images/edeqa-logo.svg" width="24" height="24">
            Edequate
        </div>
        <div class="text">
            This site requires to allow Javascript. Please enable Javascript in your browser and try again or use
            other browser that supports Javascript.
        </div>
        <div class="copyright">
            <a href="http://www.edeqa.com/edequate" class="link">Edequate</a> &copy;2017-18 <a href="http://www.edeqa.com" class="link">Edeqa</a>
        </div>
    </noscript>
    </body>
    </html>
```

Here are the next important lines.

Connect main styles:

    @import url("/css/edequate.css");
    
Connect styles if you'd like to make horizontal menu:
    
    @import url("/css/edequate-horizontal.css");

Edequate embedding:

    <script async src="/js/Edequate.js"
        data-variable="u"
        data-callback="u.require('/js/main/Main.js', u).then(function(main){main.start({type:'main'})})"
        data-export-constants="true">
    </script>

You may use variable `u` globally with this definition. Or just replace it with any other text.

`u.require('/js/main/Main.js', u)` loads the main script of project and instantiates it with Edequate's context. Next, `then(function(main){main.start({type:'main'})})`, uses the instance of `Main.js` and calls its `start` with arguments.

### Main.js

Edequate has the predefined `Main.js` which must not be modified. It takes some arguments and provides some API methods.

#### Arguments

Arguments passes into `Main.js` as an object. All keys are optional. Possible keys:

* `type` - . Default: `main`.

* `info` - 

Arguments object will be stored as the property `arguments`.

#### Properties
 
* `history` - 

* `mainType` - the same as `Arguments.type`.

* `layout` - 

* `actionbar`

* `selectLang`

* `holder` - selected holder.

* `drawer`

* `content`

* `eventBus`

* `buttonScrollTop`

* `edequate` - instance of Edequate
 
 
#### Methods

* `loadResources`

* `turn`


## Edequate.js

#### Table of contents

* [Main methods](#main-methods)
* [Actionbar](#actionbar)
* [Dialog](#dialog)
* [Drawer](#drawer)
* [Event bus](#event-bus)
* [Lang](#lang)
* [Menu](#menu)
* [Progress](#progress)
* [Table](#table)
* [Toast](#toast)
* [Tree](#tree)
* [Using Edequate.js separately](#using-edequate.js-separately)

#### Main methods

    byId
    clear
    cloneAsObject
    copyToClipboard
    create
    destroy
    fire
    get
    getJSON
    keys
    load
    loading
    loadForContext
    normalizeName
    notification
    post
    put
    require
    save


#### Main methods

#### Actionbar

#### Dialog

#### Drawer

#### Event bus

#### Lang

#### Menu

#### Progress

#### Table

#### Toast

#### Tree

#### Using Edequate.js separately

    <style>
        @import url("/css/edequate.css");
        @import url("/css/edequate-horizontal.css");
    </style>

    <script async 
       src="/js/Edequate.js" 
       data-variable="u" 
       data-callback="u.toast.show('Simple toast')" 
       data-export-constants="true"
       data-origin="edequate">
    </script>

`data-variable` [optional] defines the global variable that will be defined with `new Edequate()` after loading. Default value is `edequate`. I.e. you may access to this instantiated object using `window.edequate` (or by your choice) variable.

`data-callback` [optional] defines the function that will be called after loading; it may use `data-variable`.

`data-export-constants` [optional]  set to `true` to define `Edequate.HTML`, `Edequate.ERRORS`, `Edequate.DRAWER`, `Edequate.HIDING` as the global constants `HTML`, `ERRORS`, `DRAWER`, `HIDING`. Ignored if `data-variable` is not defined;

`data-origin` [optional] prefix for save/load values to browser's localStorage. Default value is `edequate`. Ignored if `data-variable` is not defined;

`data-context` [optional] suffix for context separated save/load values to browser's localStorage. Default value is empty. Ignored if `data-variable` is not defined;

You may also use it with [RawGit](http://rawgit.com):

    https://cdn.rawgit.com/Edeqa/Edequate/VERSION/src/main/webapp/js/Edequate.js
    https://cdn.rawgit.com/Edeqa/Edequate/VERSION/src/main/webapp/css/edequate.css
    https://cdn.rawgit.com/Edeqa/Edequate/VERSION/src/main/webapp/css/edequate-horizontal.css

Set the desired `VERSION` and use it stable. Or set `master` for always getting the fresh version. Available tags are [here](https://github.com/Edeqa/Edequate/tags).

For example:

    https://cdn.rawgit.com/Edeqa/Edequate/2.0.1/src/main/webapp/js/Edequate.js
    https://cdn.rawgit.com/Edeqa/Edequate/master/src/main/webapp/js/Edequate.js
    


## History

...

## License

Edequate Framework is licensed under an MIT license. See the `LICENSE` file for specifics.

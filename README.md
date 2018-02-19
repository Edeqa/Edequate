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


### 


### Using Edequate.js separately

    <script async 
       src="/js/Edequate.js" 
       data-variable="u" 
       data-callback="u.toast.show('Simple toast')" 
       data-export-constants="true">
    </script>

`data-variable` [optional] defines the global variable that will be defined with `new Edequate()` after loading.

`data-callback` [optional] defines the function that will be called after loading; it may use `data-variable`.

`data-export-constants` [optional]  set to `true` to define `Edequate.HTML`, `Edequate.ERRORS`, `Edequate.DRAWER`, `Edequate.HIDING` as the global constants `HTML`, `ERRORS`, `DRAWER`, `HIDING`. Ignored if `data-variable` is not defined;

`data-origin` [optional] prefix for save/load values to browser's localStorage. Default value is `edequate`. Ignored if `data-variable` is not defined;

`data-context` [optional] suffix for context separated save/load values to browser's localStorage. Default value is empty. Ignored if `data-variable` is not defined;

## License

Edequate Framework is licensed under an MIT license. See the `LICENSE` file for specifics.

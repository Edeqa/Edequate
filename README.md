# Edequate

Site building framework.

Includes javascript DOM and interface routines.

## How to use

    <script async 
       src="/js/Edequate.js" 
       variable="u" 
       callback="u.toast.show('Simple toast')" 
       exportConstants="true">
    </script>

`variable` [optional] defines the global variable that will be defined with `new Edequate()` after loading.

`callback` [optional] defines the function that will be called after loading; it may use `variable`.

`exportConstants` [optional]  set to `true` to define `Edequate.HTML`, `Edequate.ERRORS`, `Edequate.DRAWER`, `Edequate.HIDING` as the global constants `HTML`, `ERRORS`, `DRAWER`, `HIDING`. Ignored if `variable` is not defined;

`origin` [optional] prefix for save/load values to browser's localStorage. Default value is `edequate`. Ignored if `variable` is not defined;

`context` [optional] suffix for context separated save/load values to browser's localStorage. Default value is empty. Ignored if `variable` is not defined;

## License

Edequate Framework is licensed under an MIT license. See the `LICENSE` file for specifics.

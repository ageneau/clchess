(ns uci.macros)

; Case 1: Show the state of a bunch of variables.
;
;   > (inspect a b c)
;
;   a => 1
;   b => :foo-bar
;   c => ["hi" "world"]
;
; Case 2: Print an expression and its result.
;
;   > (inspect (+ 1 2 3))
;
;   (+ 1 2 3) => 6
;

(defn- inspect-1 [expr]
  `(let [result# ~expr]
     (js/console.info (str (pr-str '~expr) " => " (pr-str result#)))
     result#))

(defmacro inspect [& exprs]
  `(do ~@(map inspect-1 exprs)))

; -------------------------------------------------------------------
; BREAKPOINT macro
; (use to stop the program at a certain point,
; then resume with the browser's debugger controls)
; NOTE: only works when browser debugger tab is open

(defmacro breakpoint []
  '(do (js* "debugger;")
       nil))
                                        ; (prevent "return debugger;" in compiled javascript)


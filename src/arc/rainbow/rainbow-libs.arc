(map require-lib
  '("pprint"
    "code"
    "html"
    "srv"
    "app"
    "prompt"
    "lib/unit-test"
    "lib/parser"))

(def file-join parts
  (apply + parts))

(def qualified-path (path)
  ((java-new "java.io.File" path) 'getAbsolutePath))

;(prn "self-test:")
;(run-all-tests)

(require-by-name rainbow/ welder fsb tetris mines)
(requires start-spiral-app rainbow/spiral)

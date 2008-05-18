(sref call* (fn (the-bobo size) (* the-bobo size)) 'bobo)

(set show-failed-only t)

(set anarki-test-suite '(suite "Anarki Compatibility Tests"
  (suite "maths"
    ("Quotient"
      (quotient 15 2)
      7)
  )
  
  (suite "defcall"
    ("defcall on custom type"
      ((fn (a-bobo)
        (a-bobo 3)
      ) (annotate 'bobo 23))
    69)
  )
))

(prn (run-tests anarki-test-suite))


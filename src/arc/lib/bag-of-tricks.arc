(mac afnwith (withses . body)
  (let w (pair withses)
    `((afn ,(map car w) ,@body) ,@(map cadr w))))

(def nilfn args nil)

(mac dbg (var)
  (w/uniq gvar
    `(let ,gvar ,var
       (ero ',var 'is ,gvar)
       ,gvar)))

(mac alet (val . body)
  `(let it ,val
       ,@body
       it))

(mac nobj args ; with thanks to http://arclanguage.org/item?id=7478 and http://arclanguage.org/item?id=7480
  `(obj ,@(mappend [list _ _] args)))

(let make-def (fn ((name args . body))
                  `(= ,name (fn ,args ,@body)))
  (mac make-obj args
    `(with (,(map car args) nil)
       ,@(map make-def args)
       (nobj ,@(map car args)))))

(def index-of (x xs)
  (catch
    (afnwith (count 0 xs1 xs)
      (if (no xs1)      (throw -1)
          (caris xs1 x) (throw count)
                        (self (+ 1 count) (cdr xs1))))))

(def mksym args
  (sym (apply string args)))

(def sym+ args
  (sym (apply + (map [string _] args))))

(def upcase-initial (x)
  (case (type x)
    string (string `(,(upcase (x 0)) ,@(cdr (coerce x 'cons))))
    sym    (sym:upcase-initial:string x)))

(mac atdef (name args . body)
  `(def ,name ,args (atomic ,@body)))

(def find-in-path (file)
  (alet (string file ".arc")
    (if (file-exists it) it)))

(assign *required-libs* ())

(def require-lib (required)
  (let arc-lib string.required
    (if (no (find arc-lib *required-libs*))
      (aif (find-in-path arc-lib)
        (do (push arc-lib *required-libs*)
            (load it))
        (err:string "Didn't find " arc-lib)))))

(mac requires (func lib)
  `(def ,func args
    (require-lib ',lib)
    (apply ,func args)))

(mac require-by-name (path . funcs)
  `(do
     ,@(map [quasiquote (requires ,_ ,(string path _))] funcs)))

(def load-file (fname)
  (w/infile f (if (is (type fname) 'string) fname (coerce fname 'string))
    (awhen (readc f)
      (tostring
        (writec it)
        (whiler c (readc f) nil
          (writec c))))))

(def write-file (fname text)
  (w/outfile f fname (w/stdout f (pr text))))

(def eval-these (exprs)
  (if (acons exprs)
      (do (eval (car exprs)) (eval-these (cdr exprs)))))

(def benchmark (times fun (o verbose nil))
  (pr "warm-up   ")
  (repeat times (do (fun) (pr ".")))
  (prn)
  (pr "benchmark ")
  (with (mintime 2000000000 maxtime 0 totaltime 0 now nil)
    (for i 1 times
      (= now (msec))
      (fun)
      (let elapsed (- (msec) now)
        (zap [min _ elapsed] mintime)
        (zap [max _ elapsed] maxtime)
        (zap [+ _ elapsed] totaltime)
        (if verbose
            (prn i " . " elapsed " " (trunc:/ totaltime i 1.0))
            (pr "."))))
    (obj avg (/ totaltime times 1.0) min mintime max maxtime)))

(mac bm (times . body)
  `(benchmark ,times (fn () ,@body)))

(mac bmv (times . body)
  `(benchmark ,times (fn () ,@body) t))

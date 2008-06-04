(defmemo awt-color (r (o g) (o b)) 
  (if (is (type r) 'sym)
      (java-static-field "java.awt.Color" r)
      (java-new "java.awt.Color" r g b)))

(mac courier (font-size) 
  `(java-new "java.awt.Font" "Courier" (java-static-field "java.awt.Font" 'PLAIN) ,font-size))

(mac dim (x y) 
  `(java-new "java.awt.Dimension" ,x ,y))

(mac button (text . action)
  `(let jb (java-new "javax.swing.JButton" ,text)
    (jb 'addActionListener 
        (java-implement "java.awt.event.ActionListener" t (obj actionPerformed (fn (action-event) ,@action))))
    jb))

(def frame (left top width height title)
  (let jf (bean "javax.swing.JFrame" 'setBounds (list left top width height) 'setTitle title)
    (jf 'setLayout (java-new "javax.swing.BoxLayout" jf!getContentPane (java-static-field "javax.swing.BoxLayout" 'Y_AXIS)))
    jf))

(def panel () (bean "javax.swing.JPanel"))

(def text-field ()
  (let tf (java-new "javax.swing.JTextField")
    (with (height (tf!getMinimumSize 'getHeight)
           width (tf!getMaximumSize 'getWidth))
      (tf 'setMaximumSize (dim width height)))
    tf))

(def text-area () (java-new "javax.swing.JTextArea"))

(def scroll-pane (component) (java-new "javax.swing.JScrollPane" component))

(def box (orientation . content)
  (let the-box (java-static-invoke "javax.swing.Box" (if (is orientation 'horizontal) 'createHorizontalBox 'createVerticalBox))
    ((afn (components) 
      (if components (do
        (the-box 'add (car components))
        (self (cdr components))))) content)
    the-box))
    
(mac key-dispatcher bindings
   (let bb (pair bindings)
     `(fn (key)
          (if ,@((afn (bb1)   
                      (if bb1
                        (let (key-char body) (car bb1)
                          (cons `(is key ,key-char) (cons body (self (cdr bb1))))))) bb)))))

(mac on-char (component fun)
  `(,component 'addKeyListener (java-implement "java.awt.event.KeyListener" nil
      (obj keyTyped (fn (event) (,fun event!getKeyChar))))))

(def load-file (fname)
  (w/infile f fname
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



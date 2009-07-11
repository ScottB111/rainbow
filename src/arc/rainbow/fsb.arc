(java-import "javax.swing.tree.TreeModel")
(require-lib "rainbow/swing")
(require-lib "rainbow/welder")

(= tagged-writers!tree-node (fn (node)
  (let node-name (node!name)
    (if (find node-name ())
        node-name
        (last:tokens node-name #\/)))))

(assign pb-cache (table))

(def make-tree-node (root isleaf kidfn)
  (or= pb-cache.root
    (let children nil
      (annotate 'tree-node (make-obj
        (invalidate  ()      (wipe children))
        (name        ()      root)
        (kids        ()      (or= children (kidfn)))
        (nth         (index) ((kids) index))
        (count       ()      (aif (kids) (len it) 0))
        (leaf        ()      isleaf)
        (child-index (child)
          (index-of child (map rep (kids)))))))))

(def make-node (root)
  (make-tree-node root
                  (no:dir-exists root)
                  (if dir-exists.root
                      (fn () (map [make-node (+ root "/" _)]
                                  (dir root)))
                      nilfn)))

(def pb-tree-model ((o path "."))
  (withs (root-node (make-node path)
          listeners nil)
    (TreeModel implement t (make-obj
      (equals                  (other)          nil)
      (getRoot                 ()               root-node)
      (getChild                (node index)     (rep.node!nth index))
      (getChildCount           (node)           (rep.node!count))
      (isLeaf                  (node)           (rep.node!leaf))
      (valueForPathChanged     (tree-path node) nil)
      (getIndexOfChild         (parent child)   (rep.parent!child-index rep.child))
      (addTreeModelListener    (listener)       (push listener listeners))
      (removeTreeModelListener (listener)       (zap [rem listener _] listeners))))))

(def refresh-path-browser (tree root)
  (with (expanded-paths (tree 'getExpandedDescendants (tree 'getPathForRow 0))
         selection-paths tree!getSelectionPaths
         model (pb-tree-model root))
    (tree 'setModel model)
    (each (path node) pb-cache (rep.node!invalidate))
    (while expanded-paths!hasMoreElements
      (tree 'expandPath expanded-paths!nextElement))
    (tree 'addSelectionPaths selection-paths)))

(def delete-file (tree)
  (let node (rep tree!getSelectionPath!getLastPathComponent)
    (when (and node (no:is node!name "(arc-path)"))
      (prn "deleting " (node!name))
      (rmfile (node!name))
      (refresh-path-browser tree))))

(def fsb ((o root "."))
  (withs (f (frame 20 100 320 800 "arc-path browser")
          tree     (java-new "javax.swing.JTree")
          renderer (bean "javax.swing.JLabel")
          model    (pb-tree-model root)
          sc       (scroll-pane tree colour-scheme!background))
    (tree 'setModel model)
    (on-key-press tree
      'enter (welder (((rep tree!getSelectionPath!getLastPathComponent) 'name)))
      'delete (delete-file tree)
      'f5    (refresh-path-browser tree root))
    (f 'add sc)
    f!show))

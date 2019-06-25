(ns todomvc.components.todo-input
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(defrecord KeyDown [which])

(reacl/defclass t this [add-action]
  refs [input]

  local-state [editing ""]

  render
  (-> (u/text-input (reacl/bind-locally this)
                    {:class "new-todo"
                     :ref input
                     :placeholder "What needs to be done?"
                     :onkeydown ->KeyDown})
      (reacl/handle-actions this))

  component-did-mount
  (fn []
    (.focus (reacl/get-dom input))
    (reacl/return))

  handle-message
  (fn [msg]
    (condp instance? msg
      KeyDown
      (let [key-pressed (:which msg)
            title (helpers/trim-title editing)]
        (if (and (= key-pressed helpers/enter-key)
                 (not-empty title))
          (reacl/return :action (add-action title)
                        :local-state "")
          (reacl/return))))))

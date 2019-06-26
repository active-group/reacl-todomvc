(ns todomvc.components.todo-input
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.global :as global]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(defrecord KeyDown [which])

(reacl/defclass t this [add-action]
  refs [input]

  local-state [editing ""]

  render
  (u/text-input (reacl/bind-locally this)
                {:class "new-todo"
                 :ref input
                 :placeholder "What needs to be done?"
                 :onkeydown (u/event-message this ->KeyDown)})

  component-did-mount
  (fn []
    (let [elem (reacl/get-dom input)]
      (reacl/return :action (global/focus-action elem))))

  handle-message
  (fn [msg]
    (condp instance? msg
      KeyDown
      (let [key-pressed (:which msg)
            title (helpers/trim-title editing)]
        (if (and (= key-pressed helpers/enter-key)
                 (not-empty title))
          (-> (add-action title)
              (reacl/merge-returned (reacl/return :local-state "")))
          (reacl/return))))))

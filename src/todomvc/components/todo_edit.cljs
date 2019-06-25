(ns todomvc.components.todo-edit
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(defrecord Blur [])
(defrecord KeyDown [which])

(defn editing-text-lens
  ([v] (or v ""))
  ([o v] v))

(reacl/defclass t this editing [commit-action reset-action]
  refs [input]

  component-did-update
  (fn []
    (.focus (reacl/get-dom input))
    (reacl/return))

  render
  (-> (u/text-input (reacl/bind this editing-text-lens)
                    {:class "edit"
                     :ref input
                     :style {:display (helpers/display-elem editing)}
                     :onblur (->Blur)
                     :onkeydown ->KeyDown})
      (reacl/handle-actions this))

  handle-message
  (fn [msg]
    (condp instance? msg
      Blur
      ;; Note: when pressing Return, a blur follows after the keypress (hidden while focused)
      (if editing
        (reacl/return :action commit-action)
        (reacl/return))
      
      KeyDown
      (let [key-pressed (:which msg)]
        (condp = key-pressed
          helpers/enter-key (reacl/return :action commit-action)
          helpers/escape-key (reacl/return :action reset-action)
          
          (reacl/return))))))

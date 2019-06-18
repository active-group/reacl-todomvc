(ns todomvc.components.utils
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]))

(reacl/defclass button this [attrs label]
  render
  (dom/button (update attrs :onclick (fn [action]
                                       (fn [_]
                                         (reacl/send-message! this action))))
              label)
  handle-message
  (fn [action]
    (reacl/return :action action)))


(reacl/defclass checkbox this checked? [& [attrs]]
  validate (assert (boolean? checked?))
  
  render
  (dom/input (merge attrs
                    {:type "checkbox"
                     :checked checked?
                     :onchange (fn [e]
                                 (reacl/send-message! this
                                                      (.. e -target -checked)))}))

  handle-message
  (fn [checked?]
    (reacl/return :app-state checked?)))


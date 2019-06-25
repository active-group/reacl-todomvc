(ns todomvc.components.utils
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]))

(defn- pass-action [act]
  (reacl/return :action act))

(defn- const-action [action target]
  (when action
    (fn [_]
      (reacl/send-message! target action))))

(defn- key-action [action target]
  (when action
    (fn [e]
      (reacl/send-message! target (action (.. e -which))))))

(reacl/defclass button this [attrs label]
  render
  (dom/button (update attrs :onclick const-action this)
              label)
  
  handle-message pass-action)

(reacl/defclass label this [attrs & content]
  render
  (apply dom/label (update attrs :ondoubleclick const-action this)
         content)
  
  handle-message pass-action)

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

(defrecord Change [value])

(reacl/defclass text-input this value [attrs]
  validate (assert (string? value))
  
  render
  (dom/input (-> (merge {:type "text"} attrs)
                 (update :onblur const-action this)
                 (update :onkeydown key-action this)
                 (assoc :value value
                        :onchange (fn [e]
                                    (reacl/send-message! this (->Change (.. e -target -value)))))))
  
  handle-message
  (fn [msg]
    (if (instance? Change msg)
      (reacl/return :app-state (:value msg))
      (pass-action msg))))

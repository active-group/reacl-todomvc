(ns todomvc.core-test
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [reacl2.test-util.beta :as tu]
            [reacl2.test-util.xpath :as xp :include-macros true]
            [todomvc.core :as core]
            [todomvc.global :as global]
            [todomvc.helpers :as helpers])
  (:require-macros [cljs.test :refer (is deftest testing)]))

(deftest todoapp-state-test
  (let [c (tu/env core/todo-app {:createNodeMock (fn [elem]
                                                   (if (= "input" (.-type elem))
                                                     ::input-ref
                                                     nil))})]
    (testing "focuses an input element on mount"
      (is (= (tu/mount! c {:todos helpers/todos-empty
                           :counter 1}
                        :all)
             (reacl/return :action (global/focus-action ::input-ref)))))

    
    (testing "adds items managing the counter"
      (is (= (tu/send-message! c (core/->Add "Hello"))
             (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed false}}
                                       :counter 2}))))

    ;; Update to 1 todo item:
    (tu/update! c {:todos {:id1 {:title "Hello" :completed false}}
                   :counter 2}
                :all)
    
    (testing "updates an item"
      (is (= (tu/send-message! c (core/->Update :id1 {:title "World" :completed true}))
             (reacl/return :app-state {:todos {:id1 {:title "World" :completed true}}
                                       :counter 2}))))

    (testing "deleted an item"
      (is (= (tu/send-message! c (core/->Delete :id1))
             (reacl/return :app-state {:todos {}
                                       :counter 2}))))

    (testing "clears completed items"
      ;; Nothing completed yet:
      (is (= (tu/send-message! c (core/->ClearCompleted))
             (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed false}}
                                       :counter 2})))
      ;; update to completed state
      (tu/update! c {:todos {:id1 {:title "Hello" :completed true}}
                     :counter 2}
                  :all)
      ;; Now clears that:
      (is (= (tu/send-message! c (core/->ClearCompleted))
             (reacl/return :app-state {:todos {}
                                       :counter 2}))))


    (testing "toggles completed state"
      (is (= (tu/send-message! c (core/->ToggleAll false))
             (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed false}}
                                       :counter 2})))
      (is (= (tu/send-message! c (core/->ToggleAll true))
             (reacl/return :app-state {:todos {:id1 {:title "Hello" :completed true}}
                                       :counter 2}))))
    ))


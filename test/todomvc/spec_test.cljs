(ns todomvc.spec-test
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [reacl2.test-util.beta :as tu]
            [reacl2.test-util.xpath :as xp :include-macros true]
            [todomvc.core :as core]
            [todomvc.global :as global]
            [todomvc.helpers :as helpers])
  (:require-macros [cljs.test :refer (is deftest testing)]))

(deftest todoapp-spec-test
  ;; Note: this tests along the specification of the Todo App Functionality
  ;; https://github.com/tastejs/todomvc/blob/master/app-spec.md#functionality
  
  (let [c (tu/env core/todo-app {:createNodeMock (fn [elem]
                                                   (if (= "input" (.-type elem))
                                                     ::input-ref
                                                     nil))})]
    
    (testing "When there are no todos, #main and #footer should be hidden."
      (tu/mount! c {:todos helpers/todos-empty
                    :counter 1}
                 :all)
      (let [comp (tu/get-component c)]
        ;; we hide main and footer with a display style on the parent
        (is (some? (xp/select comp (xp/>> ** (xp/or (xp/css-class? "main") (xp/css-class? "footer")) .. (xp/style? {:display "none"})))))))

    (testing "New todos are entered in the input at the top of the app"
      (testing "The input element should be focused when the page is loaded"
        (= (tu/mount! c {:todos helpers/todos-empty
                         :counter 1}
                      :all)
           (reacl/return :action (global/focus-action ::input-ref))))

      (testing "Pressing Enter creates the todo, appends it to the todo list, and clears the input"
        (let [input (xp/select (tu/get-component c) (xp/>> ** "input" (xp/css-class? "new-todo")))]
          (is (some? input))

          (tu/invoke-callback! input :onchange #js {:target #js {:value "Foobar"}})
          (is (= (.. input -props -value)
                 "Foobar"))
          
          (is (= (tu/invoke-callback! input :onkeydown #js {:which 13})
                 (reacl/return :app-state {:todos {:id1 {:title "Foobar" :completed false}}
                                           :counter 2})))

          (is (= (.. input -props -value)
                 ""))))

      (testing "check that it's not empty before creating a new todo"
        (let [input (xp/select (tu/get-component c) (xp/>> ** "input" (xp/css-class? "new-todo")))]
          (tu/invoke-callback! input :onchange #js {:target #js {:value "   "}})
          (is (= (.. input -props -value)
                 "   "))
          
          (is (= (tu/invoke-callback! input :onkeydown #js {:which 13})
                 (reacl/return))))))
    
    (testing "checkbox toggles all the todos to the same state as itself."
      (tu/update! c {:todos {:id1 {:title "Foobar" :completed false}
                             :id2 {:title "Baz" :completed true}}
                     :counter 3}
                  :all)
      (let [mark-all (xp/select (tu/get-component c) (xp/>> ** "input" (xp/id= "toggle-all")))]
        (is (some? mark-all))

        (is (= (tu/invoke-callback! mark-all :onchange #js {:target #js {:checked true}})
               (reacl/return :app-state {:todos {:id1 {:title "Foobar" :completed true}
                                                 :id2 {:title "Baz" :completed true}}
                                         :counter 3})))
        (is (= (tu/invoke-callback! mark-all :onchange #js {:target #js {:checked false}})
               (reacl/return :app-state {:todos {:id1 {:title "Foobar" :completed false}
                                                 :id2 {:title "Baz" :completed false}}
                                         :counter 3})))

        (testing "The \"Mark all as complete\" checkbox should also be updated when single todo items are checked/unchecked"
          (tu/update! c {:todos {:id1 {:title "Foobar" :completed true}
                                 :id2 {:title "Baz" :completed true}}
                         :counter 3}
                      :all)
          (is (= (.. mark-all -props -checked)
                 true))

          (tu/update! c {:todos {:id1 {:title "Foobar" :completed false}
                                 :id2 {:title "Baz" :completed false}}
                         :counter 3}
                      :all)
          (is (= (.. mark-all -props -checked)
                 false)))))

    (testing "A todo item"
      (tu/update! c {:todos {:id1 {:title "Foobar" :completed false}
                             :id2 {:title "Baz" :completed true}}
                     :counter 3}
                  :all)
      (let [item1 (xp/select (tu/get-component c) (xp/>> ** "li" [(xp/>> ** "label" xp/text (xp/is= "Foobar"))]))
            item2 (xp/select (tu/get-component c) (xp/>> ** "li" [(xp/>> ** "label" xp/text (xp/is= "Baz"))]))]
        (is (some? item1))
        (is (some? item2))

        (testing "checkbox marks the todo as complete"
          (let [check1 (xp/select item1 (xp/>> ** "input" [:type (xp/is= "checkbox")]))]
            (is (= (tu/invoke-callback! check1 :onchange #js {:target #js {:checked true}})
                   (reacl/return :app-state {:todos {:id1 {:title "Foobar" :completed true}
                                                     :id2 {:title "Baz" :completed true}}
                                             :counter 3})))))
        (testing "toggling the class completed on its parent <li>"
          (is (nil? (xp/select item1 (xp/where (xp/css-class? "completed")))))
          (is (some? (xp/select item2 (xp/where (xp/css-class? "completed"))))))

        (testing "Double-clicking the <label> activates editing mode, by toggling the .editing class on its <li>"
          (is (nil? (xp/select item1 (xp/where (xp/css-class? "editing")))))

          (tu/invoke-callback! (xp/select item1 (xp/>> ** "label")) :ondoubleclick #js {})

          (is (some? (xp/select item1 (xp/where (xp/css-class? "editing"))))))

        (testing "Hovering over the todo shows the remove button"
          ;; done via css; not testable.
          )

        (testing "When editing mode is activated it will hide the other controls and bring forward an input that contains the todo title, which should be focused"
          (is (= (tu/invoke-callback! (xp/select item2 (xp/>> ** "label")) :ondoubleclick #js {})
                 (reacl/return :action (global/focus-action ::input-ref))))

          ;; hiding is done via css; not testable.
          )

        (testing "The edit should be saved on both blur and enter"
          ;; Note: item1 is already in editing mode by 'double-click' test above.
          (let [input (xp/select item1 (xp/>> ** "input" [:type (xp/is= "text")]))]
            (tu/invoke-callback! input "onchange" #js {:target #js {:value "Foo bar"}})
              
            (is (= (tu/invoke-callback! input "onkeydown" #js {:which 13})
                   (reacl/return :action (global/focus-action ::input-ref)
                                 :app-state {:todos {:id1 {:title "Foo bar" :completed false}
                                                     :id2 {:title "Baz" :completed true}}
                                             :counter 3})))

            (is (nil? (xp/select item1 (xp/where (xp/css-class? "editing"))))))
            
          (let [input (xp/select item2 (xp/>> ** "input" [:type (xp/is= "text")]))]
            (tu/invoke-callback! input "onchange" #js {:target #js {:value " bla "}})

            (is (= (tu/invoke-callback! input "onblur" #js {})
                   (reacl/return :action (global/focus-action ::input-ref)
                                 :app-state {:todos {:id1 {:title "Foobar" :completed false}
                                                     :id2 {:title "bla" :completed true}}
                                             :counter 3})))
            (is (nil? (xp/select item2 (xp/where (xp/css-class? "editing")))))))
          
        (testing "If it's empty the todo should instead be destroyed"
          (tu/invoke-callback! (xp/select item1 (xp/>> ** "label")) :ondoubleclick #js {})
          (let [input (xp/select item1 (xp/>> ** "input" [:type (xp/is= "text")]))]
            (tu/invoke-callback! input :onchange #js {:target #js {:value "  "}})
            (is (= (tu/invoke-callback! input "onkeydown" #js {:which 13})
                   (reacl/return :app-state {:todos {:id2 {:title "Baz" :completed true}}
                                             :counter 3})))))

        (testing "If escape is pressed during the edit, the edit state should be left and any changes be discarded."
          (tu/invoke-callback! (xp/select item1 (xp/>> ** "label")) :ondoubleclick #js {})
          (let [input (xp/select item1 (xp/>> ** "input" [:type (xp/is= "text")]))]
            (tu/invoke-callback! input :onchange #js {:target #js {:value "blub"}})
            (is (= (tu/invoke-callback! input "onkeydown" #js {:which 27})
                   (reacl/return :action (global/focus-action ::input-ref))))))
        ))
      
    (testing "A counter"
      (testing "Displays the number of active todos in a pluralized form"
        (let [counter (xp/select (tu/get-component c) (xp/>> ** "span" (xp/css-class? "todo-count")))]
          (is (some? counter))

          (is (= (xp/select counter (xp/>> / "strong" xp/text))
                 "1"))
          (is (= (xp/select counter xp/text)
                 " item left"))

          (tu/update! c {:todos {:id1 {:title "Foobar" :completed false}
                                 :id2 {:title "bla" :completed false}}
                         :counter 3}
                      :all)

          (is (= (xp/select counter (xp/>> / "strong" xp/text))
                 "2"))
          (is (= (xp/select counter xp/text)
                 " items left"))

          (tu/update! c {:todos {:id1 {:title "Foobar" :completed true}
                                 :id2 {:title "bla" :completed true}}
                         :counter 3}
                      :all)

          (is (= (xp/select counter (xp/>> / "strong" xp/text))
                 "0"))
          (is (= (xp/select counter xp/text)
                 " items left")))))

    (testing "Clear completed button"
      (let [clear (xp/select (tu/get-component c) (xp/>> ** "button" (xp/css-class? "clear-completed")))]
        (testing "Removes completed todos when clicked"
            
          (is (= (tu/invoke-callback! clear "onclick" #js {})
                 (reacl/return :app-state {:todos {}
                                           :counter 3}))))
        (testing "Should be hidden when there are no completed todos"
          (is (some? (xp/select clear (xp/style? {:display "none"})))))))

    ;; Persisence and Routing needs the wrapper todo-app-controller:
    (let [wrapper (tu/env core/todo-app-controller)
          todos-changed (fn [todos] (reacl/return :action [::todos todos]))
          counter-changed (fn [counter] (reacl/return :action [::counter counter]))]

      (testing "Persistence"
        ;; done by the wrapper todoapp-conroller together with the toplevel action handler
        (tu/mount! wrapper {:todos {:id1 {:title "Foobar" :completed false}}
                            :counter 3}
                   todos-changed
                   counter-changed)
        (let [app (xp/select (tu/get-component wrapper) (xp/>> / core/todo-app))
              new-state {:todos {:id1 {:title "Foo bar" :completed true}}
                         :counter 4}]
          (is (some? app))

          (is (= (tu/inject-return! app (reacl/return :app-state new-state))
                 (reacl/return :app-state new-state
                               :action [::todos (:todos new-state)]
                               :action [::counter (:counter new-state)])))))

      (testing "Routing"
        ;; done by secretary and goo.history (not tested here) - then a message is sent to the app to change the view (tested)
      
        (testing "When the route changes, the todo list should be filtered on a model level and the selected class on the filter links should be toggled."
          (tu/send-message! wrapper (core/->ChangeDisplay :active))
          (is (= (tu/inspect-local-state (tu/get-component wrapper))
                 {:display-type :active}))

          (is (some? (xp/select (tu/get-component wrapper) (xp/>> ** "li" / "a" [:href (xp/is= "#/")] [:class (xp/is= "")]))))
          (is (some? (xp/select (tu/get-component wrapper) (xp/>> ** "li" / "a" [:href (xp/is= "#/active")] [:class (xp/is= "selected")])))))))
    
    ))

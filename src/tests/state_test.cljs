(ns tests.state-test
  (:require [cljs.test :refer (deftest is)]
            [game.state :as state]
            [game.actions :as actions]
            [game.builders :as builders]))

(deftest use-stat-change-potion-that-heals-player
  (let [heal-effect (builders/build-stat-change-effect {:stat :health :amount 10})
        heal-potion (builders/build-potion {:id :1 :effects [heal-effect] :quantity 3})]

    (reset! state/entities {:player (builders/build-player {:health 50 :items {:1 heal-potion}})})

    (actions/use-item :player :1)
    (is (= (state/get-player) (builders/build-player {:health 60 :items {:1 (merge heal-potion {:quantity 2})}})))))

(deftest stat-change-cant-exceed-max-health
  (let [heal-effect (builders/build-stat-change-effect {:stat :health :amount 100})
        heal-potion (builders/build-potion {:id :1 :effects [heal-effect] :quantity 3})]

    (reset! state/entities {:player (builders/build-player {:health 50 :max-health 100 :items {:1 heal-potion}})})

    (actions/use-item :player :1)
    (is (= (state/get-player) (builders/build-player {:health 100 :max-health 100 :items {:1 (merge heal-potion {:quantity 2})}})))))

(deftest using-last-item-removes-it-from-items
  (let [heal-effect (builders/build-stat-change-effect {:stat :health :amount 10})
        heal-potion (builders/build-potion {:id :1 :effects [heal-effect] :quantity 1})]

    (reset! state/entities {:player (builders/build-player {:health 50 :items {:1 heal-potion}})})

    (actions/use-item :player :1)
    (is (= (state/get-player) (builders/build-player {:health 60 :items {}})))))



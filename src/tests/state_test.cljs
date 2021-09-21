(ns tests.state-test
  (:require [cljs.test :refer (deftest run-tests are is)]
            [game.state :as state]
            [game.builders :as builders]))

(deftest use-stat-change-potion-that-heals-player
  (let [heal-effect (builders/build-stat-change-effect {:stat :health :amount 10})
        heal-potion (builders/build-potion {:id :1 :effects [heal-effect] :quantity 3})]

    (reset! state/items {:1 heal-potion})
    (reset! state/entities {:player (builders/build-player {:health 50})})

    (state/use-item :player :1)
    (is (= @state/items {:1 (merge heal-potion {:quantity 2})}))
    (is (= (state/get-player) (builders/build-player {:health 60})))))

(deftest stat-change-cant-exceed-max-health
  (let [heal-effect (builders/build-stat-change-effect {:stat :health :amount 100})
        heal-potion (builders/build-potion {:id :1 :effects [heal-effect] :quantity 3})]

    (reset! state/items {:1 heal-potion})
    (reset! state/entities {:player (builders/build-player {:health 50 :max-health 100})})

    (state/use-item :player :1)
    (is (= (state/get-player) (builders/build-player {:health 100 :max-health 100})))))


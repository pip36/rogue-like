(ns game.core
  (:require
   [game.canvas :as canvas]
   [game.state :as s]
   [game.config :as config]
   [game.builders :as builders]))

(defn render []
  (when (not (= :GAME_OVER @s/game-state))
    (canvas/clear)
    (s/render-map)
    (s/render-entities)))

(defn register-input-listener []
  (set! (. js/document -onkeydown)
        (fn [e]
          (let [k (config/input-keys (. e -keyCode))]
            (when (not (= :GAME_OVER @s/game-state))
              (s/handle-user-update k)
              (render)
              (s/update-monsters)
              (when (not (contains? @s/entities :player)) (s/end-game))
              (render))))))

(defn populate-map []
  (reset! s/entities {:player (builders/build-player {:x 2
                                                      :y 3})
                      :1 (builders/build-jelly {:id :1 :x 13 :y 13})
                      :2 (builders/build-jelly {:id :2 :x 1 :y 1})
                      :3 (builders/build-jelly {:id :3 :x 6 :y 6})
                      :4 (builders/build-statue {:id :4 :x 9 :y 10})}))

(defn start-game []
  (canvas/init-ctx)
  (register-input-listener)
  (populate-map)
  (render))

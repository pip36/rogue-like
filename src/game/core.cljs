(ns game.core
  (:require
   [game.canvas :as canvas]
   [game.tile-map :as m]
   [game.player :as p]
   [game.input :as i]
   [game.ai :as ai]))

(defn render []
  (canvas/clear)
  (m/render-map)
  (p/render-player))

(defn register-input-listener []
  (set! (. js/document -onkeydown)
        (fn [e]
          (let [k (i/input-keys (. e -keyCode))]
            (i/handle-user-update k)
            (render)
            (ai/handle-ai-update)
            (render)))))

(defn start-game []
  (canvas/init-ctx)
  (register-input-listener)
  (render))

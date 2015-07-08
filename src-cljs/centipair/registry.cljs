(ns centipair.registry
  (:require [centipair.core.user.forms :as user-forms]
            [centipair.movies.dvd :as dvd]
            ))


(def function-registry {:render-register-form user-forms/render-register-form
                        :render-login-form user-forms/render-login-form
                        :render-forgot-password-form user-forms/render-forgot-password-form
                        :render-reset-password-form user-forms/render-reset-password-form
                        :render-movie-list dvd/render-movie-list})



(defn ^:export load-function [name]
  (((keyword name) function-registry)))

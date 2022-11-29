package com.example.gymline

data class User(var firstName : String ?= null, var lastName : String ?= null,
                var gender : String ?= null, var birthdate : String ?= null,
                var weight : String ?= null, var height : String ?= null,){
}
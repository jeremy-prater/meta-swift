cmake_minimum_required(VERSION 3.16.3)

project(HelloWorld
  LANGUAGES C Swift)

add_executable(hello main.swift)

install(TARGETS hello RUNTIME DESTINATION bin)

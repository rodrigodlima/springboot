FROM docker.io/ubuntu
CMD bash -c "while true; do (( i++ )); echo test \$i; sleep 5; done"
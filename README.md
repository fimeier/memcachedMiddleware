# asl-fall19-project (fimeier@student.ethz.ch)
....touched by Ubi-VM-P52!
....touched by WSL-P52......
# Content
- [asl-fall19-project (fimeier@student.ethz.ch)](#asl-fall19-project-fimeierstudentethzch)
- [Content](#content)
- [Commands for Job Controlling](#commands-for-job-controlling)

# Commands for Job Controlling
screen -S jobcontrolling
java -jar ~/asl-fall18-project/dist/ASLJobControlling.jar > outputASLJOB 2>errorASLJOB 

screen -dR jobcontrolling



ssh 10.0.0.31
screen -S experiment
cd automato; ./start_memcached.bash 12333

ssh 10.0.0.32
screen -S experiment
cd automato; ./start_memcached.bash 12444


ssh 10.0.0.33
screen -S experiment
cd automato; ./start_memcached.bash 12555




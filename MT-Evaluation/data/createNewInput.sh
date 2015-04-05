#!/bin/bash


i=1;

while read line  
do  
	echo $i" ||| "$line; 
	(( i++ )); 
done < ./hyp1-hyp2-ref  > new-hyp1-hyp2-ref2



#i=1;while read line  ; do  echo $i" ||| "$line; (( i++ )); done < ./hyp1-hyp2-ref  > new-hyp1-hyp2-ref2

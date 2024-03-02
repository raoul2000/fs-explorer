
echo ""
echo "starting : build release app (jdk-17.0.1)" 
echo ""

export PATH="/c/Program Files/Java/jdk-17.0.1/bin:$PATH"
npx shadow-cljs release app 
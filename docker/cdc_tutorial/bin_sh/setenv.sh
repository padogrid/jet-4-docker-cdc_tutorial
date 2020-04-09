#
# Enter app specifics in this file.
#

# Cluster level variables:
# ------------------------
# BASE_DIR - hazelcast-addon base dir
# ETC_DIR - Cluster etc dir
# LOG_DIR - Cluster log dir

# App level variables:
# --------------------
# APPS_DIR - <hazelcast-addon>/apps dir
# APP_DIR - App base dir
# APP_ETC_DIR - App etc dir
# APP_LOG_DIR - App log dir

# Set JAVA_OPT to include your app specifics.
#JAVA_OPTS=

# Set CLASSPATH
CLASSPATH="$CLASSPATH:$APP_DIR/target/*"
if [ ! -d "$APP_LOG_DIR" ]; then
   mkdir -p "$APP_LOG_DIR"
fi
export LOG_FILE=$APP_LOG_DIR/read_cache.log

JAVA_OPTS="$JAVA_OPTS -Dhazelcast.client.config=$HAZELCAST_CLIENT_CONFIG_FILE"

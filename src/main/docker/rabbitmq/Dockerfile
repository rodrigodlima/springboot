FROM rabbitmq:3.6.12-management

RUN apt-get update

#COPY plugins/rabbitmq_message_timestamp-3.6.x-3195a55a.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.7.3/plugins/

#RUN rabbitmq-plugins enable rabbitmq_message_timestamp
RUN apt-get install -y curl

RUN curl http://www.rabbitmq.com/community-plugins/v3.6.x/rabbitmq_delayed_message_exchange-0.0.1.ez > $RABBITMQ_HOME/plugins/rabbitmq_delayed_message_exchange-0.0.1.ez

RUN rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
RUN rabbitmq-plugins enable --offline rabbitmq_consistent_hash_exchange


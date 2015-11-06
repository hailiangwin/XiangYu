package tk.wenop.XiangYu.bean;

import java.util.List;

import cn.bmob.v3.BmobObject;

    public class CommentEntity extends BmobObject {
        private String comment;
        private User toUser;
        private User ownerUser;
        //對消息的評論
        private MessageEntity ownerMessage;
        //对评论的评论
        private CommentEntity owerComment;

        private Boolean anonymous;

        private List<CommentEntity> myComments;




        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public void setToUser(User toUser) {
            this.toUser = toUser;
        }
        public User getToUser() {
            return toUser;
        }
        public void setOwnerUser(User ownerUser) {
            this.ownerUser = ownerUser;
        }
        public User getOwnerUser() {
            return ownerUser;
        }
        public void setOwnerMessage(MessageEntity ownerMessage) {
            this.ownerMessage = ownerMessage;
        }
        public MessageEntity getOwnerMessage() {
            return ownerMessage;
        }

        public Boolean getAnonymous() {
            return anonymous;
        }

        public void setAnonymous(Boolean anonymous) {
            this.anonymous = anonymous;
        }

        public List<CommentEntity> getMyComments() {
            return myComments;
        }

        public void setMyComments(List<CommentEntity> myComments) {
            this.myComments = myComments;
        }

        public CommentEntity getOwerComment() {
            return owerComment;
        }

        public void setOwerComment(CommentEntity owerComment) {
            this.owerComment = owerComment;
        }
    }
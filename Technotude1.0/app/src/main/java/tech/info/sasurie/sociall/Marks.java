package tech.info.sasurie.sociall;

public class Marks {
  private  String fullname,mark,profileimage;

    public Marks(String fullname, String mark, String profileimage) {
        this.fullname = fullname;
        this.mark = mark;
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}

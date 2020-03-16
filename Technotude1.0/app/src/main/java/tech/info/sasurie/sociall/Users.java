package tech.info.sasurie.sociall;

/**
 * Created by Linus on 16/09/18.
 */

public class Users
{
    public String fullname,status,profileimage;
    private int mark;

    public Users()
    {


    }


    public Users(String fullname, String status, String profileimage,int mark)
    {
        this.mark=mark;
        this.fullname = fullname;
        this.status = status;
        this.profileimage = profileimage;
    }
    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }


    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}

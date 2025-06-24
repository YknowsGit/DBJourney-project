package hello.jdbc.connection;

// 상수를 모아논것 => 객체 생성 X => abstract로 객체 생성 막음
public abstract class ConnectionConst {
    public static final String URL = "jdbc:h2:tcp://localhost/~/test2";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}

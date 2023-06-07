WebServer_V2:本版本开始完成解析请求的工作

    HTTP协议要求客户端(浏览器)与服务端之间的交互方式必须采取一问一答的模式,即:客户端发送请求,服务端处理后予以响应

    上个版本中经过测试发现:客户端连接服务端后就自动发送了请求信息过来.因此我们在处理请求前,要先将这些信息分类整理好,以便后续处理

    分析请求的格式发现,请求行和消息头部分都是文本数据,并且有一个共同特点(都是一行字符串,都以回车符换行符这两个字符连续结尾的)
    因此解析请求前,我们先测试一个操作,读取一行字符串


WebServer_V3:本版本继续完成解析请求的工作

    HTTP协议中一个请求由3部分构成,因此我们设计一个类HttpRequest,将请求的各个部分内容作为属性定义出来
    解析请求时,我们将客户端发送的请求最终以一个HttpRequest的实例表示出来,以便后续处理请求时,可以通过该对象获取请求中各个部分内容

    如何实现:
        1.新建一个包:com.webserver.http,该包用于存放所有与HTTP协议有关的内容
        2.在http包中新建类:HttpRequest(请求对象)
        3.在ClientHandler中处理流程中完成第一步:解析请求,该工作实际就是实例化HttpRequest对象,具体的解析工作交给HttpRequest自行完成
        4.完成HttpRequest构造方法,在其中解析


WebServer_V4:本版继续完成解析请求的工作

    上一个版本中我们已经解析完请求行了,本版继续解析消息头(完成HttpRequest的parseHeaders方法)


WebServer_v5:此版本开始完成第二步处理请求的工作

    ClientHandler:在上一个版本中通过实例化HttpRequest完成解析请求的工作.接着我们要根据对象获取客户端发送过来的请求内容来处理请求

    这里我们以浏览器请求一个页面为例,来完成处理工作.因此我们先准备一个页面,以便于后续操作

    如何实现:
        1.在当前项目目录下新建一个目录:webapps,这个目录用户保存所有维护的网络应用(webapp)
        2.在webapps目录下新建一个子目录:myweb,该目录作为一个webapp的名字,里面包含这个应用中所有的内容(页面、图片、其他资源等)
          将来需要做其他应用时也这样创建其他子目录即可
        3.在myweb目录下新建一个页面:index.html

    页面完成后,我们在浏览器地址栏中,输入http://localhost:8088/myweb/index.html,此时服务端收到请求时,在请求行的抽象路径部分(uri)的值为:/myweb/index.html
    我们在webapps目录下再根据这个路径应当可以找到该资源

    如何实现:
        1.在ClienHandler处理环节第二步中首先通过HttpRequest获取请求行中的抽象路径部分
        2.根据用户请求的资源的抽象路径去webapps目录下寻找该文件并根据是否找到分支做不同的打桩


WebServer_v6:此版本此版本完成第三步:响应客户端

    上个版本中我们在ClientHandler中根据用户请求中的抽象路径到webapps目录下寻找资源并进行分支打桩

    本版本我们先处理找到资源的情况,如果该资源找到,我们应当将该资源响应给客户端
    若想回复客户端,我们需要按照Http协议要求,发送一个标准的响应(Response)给客户端才能让其正确显示

WebServer_v7:此版本完成响应404页面

    当浏览器输入一个错误的地址,导致服务端根据该抽象路径找不到对应的资源时,我们应当给客户端回复404页面

    如何实现:
        1.在webapps目录下新建一个子目录root
        2.在root目录下新建一个页面404.html,这个页面之所以不创建在myweb目录中是因为404页面是一个公共页面,浏览器无论请求哪个网络应用下的资源,只要不存在都应当响应该页面
        3.在ClientHandler处理请求的环节中,资源不存在分支下完成响应404页面的操作,发送响应的内容是:
            状态行内容:HTTP/1.1 404 NOT FOUND
            响应头还是Content-Type和Content-Length
            响应正文则是将404页面内容发送给客户端


WebServer_v8:此版本对响应客户端部分进行封装

    上一个版本实现了响应正确页面和404页面,但是代码有重复,因此我们需要封装发送响应的部分
    响应的格式与请求的格式类似,因此同样设计一个类:HttpResponse,使用这个类的实例来表示一个具体发送给客户端的响应内容.然后统一方式发送

    如何实现:
        1.在com.webserver.http包中新建类:HttpResponse
        2.定义flush方法,用于发送当前响应内容
        3.在ClientHandler中先使用这个对象测试响应正确资源的情况


WebServer_v9:此版本完成浏览器发送多次请求的需求

    本版本一开在index.html页面上加入一个图片标签,发现无法正常显示,原因在于页面上有其他素材时浏览器会发送多次请求来获取这些资源并显示到页面中
    因此,我们的服务端需要支持多次请求与响应,按照HTTP1.0协议要求处理时,要求客户端与服务端建立TCP连接后允许一次请求与响应的交互后就要断开连接

    如何实现:
        1.修改ClientHandler类中run方法,在整个try-catch代码块的最后添加finally代码块,在其中调用socket.close()与客户端断开TCP连接
        2.在WebServer主类的start方法中将等待客户端连接并进行处理的操作使用while死循环的方式重复执行,从而服务端即可接收客户端的多次请求

    导入学子商城资源后,访问其首页会出现页面无法正常显示的情况,跟踪请求发现所有资源都已经响应完成,但是由于指定的响应头Content-Type是写死的,固定发送text/html
    这导致浏览器不能正确理解其请求的资源从而出现显示不正确的情况,因此我们要将响应头发送的代码进行改造,最终根据实际请求的资源的类型响应对应的Conent-Type的值
    并且也要做到发送响应头是可以设置的,而不是固定的只发送Content-Type和Content-Length

    解决可以根据设置的响应头进行发送响应:在HttpResponse中添加一个Map属性,用来保存所有要给客户端发送的响应头,并且在sendHeaders方法中遍历这个Map将所有响应头发送


WebServer_v10:此版本重构响应

    此版本我们要根据客户端实际请求的资源类型响应正确的Content-Type的值
    不同的资源类型对应的Content-Type值是不同的,常见的类型:
        html    text/html
        css     text/css
        js      application/javascript
        png     image/png
        gif     image/gif
        jpg     image/jpeg

WebServer_v11:此版本继续重构响应

    上一个版本我们成功实现了根据请求的资源按照其实际类型响应正确的Content-Type了,因此学子商城页面可以正常显示
    我们在往response中设置响应内容后要为其添加两个响应头Contnet-Type与Contnet-Length,不便于代码的理解,所以我们将设置响应头的操作放在设置响应内容的方法中

WebServer_v12:此版本继续重构响应

    上一个给版本中出现一个问题,每次设置响应正文时根据正文设置响应头Content-Type都要实例化一个Map,而这个Map的内容是固定不变的
    因此我们可以将这个Map定义为一个静态的,每次获取值即可

    如何实现:
        1.在com.webserver.http包中定义一个类:HttpContext类,这个类定义所有有关HTTP协议规定的不会变的内容
            将来程序中有需要用到HTTP协议规定内容都从这里获取即可
        2.在HttpContext类中定义一个静态Map属性,保存所有Content-Type的值
        3.定义初始化操作和根据后缀获取类型的方法


WebServer_v13:此版本继续重构响应

    本版本我们支持所有的介质类型,上一个版本中对于资源对应的Content-Type我们仅仅支持6个
    这里我们引用Tomcat整理的所有类型(在Tomcat安装目录下的conf目中有一个web.xml文件)来初始化,使得我们也能支持这1000多个类型

    如何实现:
       1.将web.xml文件拷贝到resources目录下
       2.修改HttpContext初始化mimeMapping这个Map的方法,改为通过解析Web.xml文件初始化


WebServer_v14:此版本解决空请求问题

    HTTP协议允许客户端发送空请求:客户端连接后没有发送任何内容而是直接与客户端断开了连接.
    此时我们服务器若感召标准流程解析请求时,在读取请求行后获取其中三部分时就会出现下标越界,导致后续操作出现其他异常
    对于空请求而言,我们接收到后应当直接忽略后续所有操作,直接与客户端断开连接即可

    如何实现:当解析请求的请求行时,如果遇到空请求,则抛出一个异常给ClientHandler,使其忽略后续一切操作,直接与客户端断开连接

WebServer_v15:此版本开始完成业务的处理

    以注册业务为例,完成服务端的统一处理流程,如下:
        1.用户访问注册页面,并输入注册信息,点击注册按钮后提交注册信息
        2.服务端通过解析请求获取用户提交的信息
        3.处理请求的环节根据用户请求及提交的数据完成相应的业务处理
        4.响应客户端业务处理结果页面

    如何实现:
        1.在webapps/myweb目录下新建注册页面reg.html,并在页面中添加表单,以便将用户输入的数据提交给服务端,在表单中我们指定提交路径"./reg"
        2.提交表单后,发现表单数据伴随请求一起出现在URL中,因此服务端解析请求行的抽象路径部分时是包含请求与参数两部分的
            对此我们在解析请求中要对这种含有参数的抽象路径进一步解析,在HttpRequest中再添加三个属性:
                String requestURI:保存uri的请求部分("?"左侧内容)
                String queryString:保存uri的参数部分("?"右侧内容)
                Map parameters:保存每一个参数
            在解析请求行的方法中获得uri的值后进一步解析,并对上面三个新属性赋值


WebServer_v16:此版本继续完成业务的处理

    上一个版本中,我们已经再HttpRequest中支持了解析页面表单提交的数据,此版本开始完成处理业务的操作

    如何实现:
        1.首先为HttpRequest中新添加的三个属性提供的get方法,以便获取其中信息
        2.在ClientHandler类中处理请求的环节,获取请求路径的操作由原有的获取uri改为获取requestURI,注意:因为uri可能含有参数,不再适合作为请求路径部分使用
        3.添加新的分支,首先判断请求路径是否为请求业务,是则创建对应的业务处理类并处理该业务,否则走原有的处理请求环节(去webapps目录下寻找该资源文件)


WebServer_v17:此版本完成登陆业务处理

    登陆业务流程:
        1.用户访问登陆页面,并输入用户名及密码,点击登陆按钮
        2.数据提交到服务端后,服务端处理登陆操作
        3.响应登陆结果页面(登陆成功或失败)

    如何实现:
        1.在webapps/myweb目录下定义相关页面:
            1.1 login.html:登陆页面,该页面表单中要求两个输入框,用户名和密码,然后表单提交路径action="./login"
            1.2 login_success.html:登陆成功提示页面,提示一句话:登录成功,欢迎回来
            1.3 login_fail.html:登录失败提示页面,提示一句话:登录失败,用户名或密码不正确
        2.在com.webserver.servlet包中定义处理类:LoginServlet,并定义service方法用来处理登良路业务
            登录业务:使用用户输入的用户名与密码去user.dat文件中顺序比较每个用户,只有都符合时才响应登录成功页面,否则响应登录失败页面
        3.在ClientHandler处理业务的环节再添加一个分支,判断请求路径的值是否为"/myweb/login",如果是则实例化LoginServlet并调用其service方法处理登录业务
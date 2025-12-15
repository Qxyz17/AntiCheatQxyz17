package com.qxyz17.acq.web;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.qxyz17.acq.manager.ConfigManager;
import com.qxyz17.acq.manager.DetectionManager;
import org.bukkit.Bukkit;

public class WebPanel {

    private final Javalin app;
    private final ConfigManager cm;
    private final DetectionManager dm;

    public WebPanel(ConfigManager cm, DetectionManager dm, int port) {
        this.cm = cm;
        this.dm = dm;
        this.app = Javalin.create(cfg -> {
            cfg.staticFiles.add("/web", Location.CLASSPATH);
        }).start(port);
        routes();
    }

    private void routes() {
        app.get("/", ctx -> ctx.redirect("/index.html"));
        app.get("/api/info", ctx -> ctx.json(dm.getStats()));
        app.post("/api/toggle/:module", ctx -> {
            String m = ctx.pathParam("module");
            boolean s = dm.toggleModule(m);
            ctx.json("{\"status\":\"" + (s ? "on" : "off") + "\"}");
        });
        app.post("/api/vl", ctx -> {
            String player = ctx.formParam("player");
            int vl = Integer.parseInt(ctx.formParam("vl"));
            dm.setVL(player, vl);
            ctx.json("{\"ok\":true}");
        });
        app.post("/api/lang", ctx -> {
            String lang = ctx.formParam("lang");
            cm.setLang(lang);
            ctx.json("{\"ok\":true}");
        });
    }

    public void stop() { app.stop(); }
}

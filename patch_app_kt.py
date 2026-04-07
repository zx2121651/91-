import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt"
with open(file_path, "r") as f:
    content = f.read()

# 在 composable(Screen.Discover.route) 中添加 onNavigateToPublish 参数
if "MainFeedScreen(onNavigateToMasquerade = { navController.navigate(\"masquerade\") })" in content:
    content = content.replace(
        "MainFeedScreen(onNavigateToMasquerade = { navController.navigate(\"masquerade\") })",
        "MainFeedScreen(onNavigateToMasquerade = { navController.navigate(\"masquerade\") }, onNavigateToPublish = { navController.navigate(\"publish_video\") })"
    )

# 在 NavHost 的末尾添加 publish_video 路由
publish_route = """
            composable("publish_video") {
                PublishVideoScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
"""

if "composable(\"publish_video\")" not in content:
    # 替换最后一个闭合括号结构
    content = content.replace(
        """
            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
""",
        """
            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
            composable("publish_video") {
                PublishVideoScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
"""
    )

with open(file_path, "w") as f:
    f.write(content)
